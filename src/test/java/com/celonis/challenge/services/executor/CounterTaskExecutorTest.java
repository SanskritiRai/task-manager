package com.celonis.challenge.services.executor;

import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.model.ProjectGenerationTaskRepository;
import com.celonis.challenge.model.TaskStatus;
import com.celonis.challenge.model.TaskType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CounterTaskExecutorTest {

    @Mock
    private ProjectGenerationTaskRepository projectGenerationTaskRepository;

    @InjectMocks
    private CounterTaskExecutor counterTaskExecutor;

    private ProjectGenerationTask projectGenerationTask;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        projectGenerationTask = new ProjectGenerationTask();
        projectGenerationTask.setId("async-1");
        projectGenerationTask.setName("async counter task test-1");
        projectGenerationTask.setTaskType(TaskType.COUNTER);
        projectGenerationTask.setTaskStatus(TaskStatus.PENDING);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("x", "1");
        parameters.put("y", "3");

        projectGenerationTask.setParameters(parameters);
    }

    @Test
    void getSupportedTaskType() {
        assertEquals(TaskType.COUNTER, counterTaskExecutor.getSupportedTaskType());
    }

    @Test
    void execute_shouldSetTaskRunningImmediately() {
        when(projectGenerationTaskRepository.save(projectGenerationTask)).thenReturn(projectGenerationTask);

        counterTaskExecutor.execute(projectGenerationTask);

        verify(projectGenerationTaskRepository).save(projectGenerationTask);
        assertEquals(TaskStatus.RUNNING, projectGenerationTask.getTaskStatus());
        assertEquals(0, projectGenerationTask.getProgress());
    }

    @Test
    void execute_shouldEventuallyCompleteTask() throws InterruptedException {
        when(projectGenerationTaskRepository.findById(projectGenerationTask.getId())).thenReturn(Optional.of(projectGenerationTask));
        counterTaskExecutor.execute(projectGenerationTask);

        waitUntil(() -> projectGenerationTask.getTaskStatus() == TaskStatus.COMPLETED);
        assertEquals(TaskStatus.COMPLETED, projectGenerationTask.getTaskStatus());
        assertEquals(100, projectGenerationTask.getProgress());
    }

    private void waitUntil(Check condition) throws InterruptedException {
        int retries = 20;
        while (retries-- > 0) {
            if (condition.ok()) {
                return;
            }
            Thread.sleep(200);
        }
        fail("Async condition not met within time");
    }

    @FunctionalInterface
    interface Check {
        boolean ok();
    }


    @Test
    void cancel() throws InterruptedException {
        when(projectGenerationTaskRepository.save(projectGenerationTask)).thenReturn(projectGenerationTask);
        when(projectGenerationTaskRepository.findById(projectGenerationTask.getId())).thenReturn(Optional.of(projectGenerationTask));

        projectGenerationTask.getParameters().put("x", "1");
        projectGenerationTask.getParameters().put("y", "10");

        counterTaskExecutor.execute(projectGenerationTask);

        Thread.sleep(300);

        counterTaskExecutor.cancel(projectGenerationTask);

        waitUntil(() -> projectGenerationTask.getTaskStatus() == TaskStatus.CANCELED || projectGenerationTask.getTaskStatus() == TaskStatus.FAILED);
        Assertions.assertNotEquals(TaskStatus.COMPLETED, projectGenerationTask.getTaskStatus());
        assertEquals(TaskStatus.CANCELED, projectGenerationTask.getTaskStatus());
    }
}