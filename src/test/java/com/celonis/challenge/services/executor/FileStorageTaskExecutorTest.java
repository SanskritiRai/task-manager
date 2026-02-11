package com.celonis.challenge.services.executor;

import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.model.ProjectGenerationTaskRepository;
import com.celonis.challenge.model.TaskStatus;
import com.celonis.challenge.model.TaskType;
import com.celonis.challenge.services.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FileStorageTaskExecutorTest {

    @Mock
    ProjectGenerationTaskRepository projectGenerationTaskRepository;

    @Mock
    FileService fileService;

    FileStorageTaskExecutor fileStorageTaskExecutor;

    ProjectGenerationTask projectGenerationTask;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        fileStorageTaskExecutor = new FileStorageTaskExecutor(fileService, projectGenerationTaskRepository);
        projectGenerationTask = new ProjectGenerationTask();
        projectGenerationTask.setId("File task-1");
        projectGenerationTask.setName("File task-1");
        projectGenerationTask.setTaskType(TaskType.STORE_FILE);
    }

    @Test
    void getSupportedTaskType() {
        assertEquals(TaskType.STORE_FILE, fileStorageTaskExecutor.getSupportedTaskType());
    }

    @Test
    void executeSuccessFlow() throws IOException {
        fileStorageTaskExecutor.execute(projectGenerationTask);

        verify(projectGenerationTaskRepository, atLeastOnce()).save(projectGenerationTask);
        verify(fileService).storeResult(eq(projectGenerationTask), any());
        assertEquals(TaskType.STORE_FILE, projectGenerationTask.getTaskType());
        assertEquals("File task-1", projectGenerationTask.getName());
        assertEquals(100, projectGenerationTask.getProgress());
        assertEquals(TaskStatus.COMPLETED, projectGenerationTask.getTaskStatus());
    }

    @Test
    void cancelForIncompleteTask() throws IOException {
        projectGenerationTask.setTaskStatus(TaskStatus.RUNNING);
        fileStorageTaskExecutor.cancel(projectGenerationTask);

        assertEquals(TaskStatus.CANCELED, projectGenerationTask.getTaskStatus());
        verify(projectGenerationTaskRepository, atLeastOnce()).save(projectGenerationTask);
    }

    @Test
    void cancelForCompletedTask() throws IOException {
        projectGenerationTask.setTaskStatus(TaskStatus.COMPLETED);

        fileStorageTaskExecutor.cancel(projectGenerationTask);

        assertEquals(TaskStatus.COMPLETED, projectGenerationTask.getTaskStatus());
        verify(projectGenerationTaskRepository, never()).save(projectGenerationTask);
    }
}