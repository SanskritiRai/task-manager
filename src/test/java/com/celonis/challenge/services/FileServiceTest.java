package com.celonis.challenge.services;

import com.celonis.challenge.exceptions.BadRequestException;
import com.celonis.challenge.exceptions.InternalException;
import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.model.ProjectGenerationTaskRepository;
import com.celonis.challenge.model.TaskStatus;
import com.celonis.challenge.model.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class FileServiceTest {

    @Mock
    private ProjectGenerationTaskRepository projectGenerationTaskRepository;

    private FileService fileService;

    private ProjectGenerationTask projectGenerationTask;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        fileService = new FileService(projectGenerationTaskRepository);

        projectGenerationTask = new ProjectGenerationTask();
        projectGenerationTask.setId("task-1");
        projectGenerationTask.setTaskType(TaskType.STORE_FILE);
    }

    @Test
    void getTaskResultWrongTaskType() {
        projectGenerationTask.setTaskType(TaskType.COUNTER);

        assertThrows(
                BadRequestException.class,
                () -> fileService.getTaskResult(projectGenerationTask)
        );
    }

    @Test
    void getTaskResultForPendingTask() {
        projectGenerationTask.setTaskStatus(TaskStatus.PENDING);

        ResponseEntity<FileSystemResource> response =
                fileService.getTaskResult(projectGenerationTask);

        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void getprojectGenerationTaskResultForMissingFile() {
        projectGenerationTask.setTaskStatus(TaskStatus.COMPLETED);
        projectGenerationTask.setStorageLocation("/no/such/file");

        assertThrows(
                InternalException.class,
                () -> fileService.getTaskResult(projectGenerationTask)
        );
    }

    @Test
    void getTaskResultForExistingFile() throws Exception {
        File temp = File.createTempFile("test", ".rtf");
        try (FileWriter fw = new FileWriter(temp)) {
            fw.write("hello");
        }

        projectGenerationTask.setTaskStatus(TaskStatus.COMPLETED);
        projectGenerationTask.setStorageLocation(temp.getAbsolutePath());

        ResponseEntity<FileSystemResource> response =
                fileService.getTaskResult(projectGenerationTask);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getFile().exists());
    }

    @Test
    void storeResult() throws Exception {
        URL url = getClass().getClassLoader().getResource("FileTest.rtf");

        assertNotNull(url, "FileTest.rtf must be present in src/test/resources");

        fileService.storeResult(projectGenerationTask, url);
        File file = new File(url.getPath());

        assertNotNull(projectGenerationTask.getStorageLocation());
        assertTrue(new File(projectGenerationTask.getStorageLocation()).exists());
        verify(projectGenerationTaskRepository).save(projectGenerationTask);
    }
}
