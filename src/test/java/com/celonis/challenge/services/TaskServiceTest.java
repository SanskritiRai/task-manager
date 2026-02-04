package com.celonis.challenge.services;

import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.model.ProjectGenerationTaskRepository;
import com.celonis.challenge.model.TaskStatus;
import com.celonis.challenge.model.TaskType;
import com.celonis.challenge.services.executor.TaskExecutor;
import com.celonis.challenge.services.executor.TaskExecutorRegistry;
import com.celonis.challenge.services.validator.TaskValidator;
import com.celonis.challenge.services.validator.TaskValidatorRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private ProjectGenerationTaskRepository projectGenerationTaskRepository;

    @Mock
    private TaskValidatorRegistry taskValidatorRegistry;

    @Mock
    private TaskExecutorRegistry taskExecutorRegistry;

    @Mock
    private TaskValidator taskValidator;

    @Mock
    TaskExecutor taskExecutor;

    @InjectMocks
    private TaskService taskService;

    @Test
    void listTasks() {
        ProjectGenerationTask projectGenerationTask1 = new ProjectGenerationTask();
        projectGenerationTask1.setId("abc1");
        projectGenerationTask1.setName("testing task 1");
        projectGenerationTask1.setProgress(50);
        projectGenerationTask1.setTaskType(TaskType.STORE_FILE);

        Map<String, String> params = new HashMap<>();
        params.put("x", "1");
        params.put("y", "100");

        ProjectGenerationTask projectGenerationTask2 = new ProjectGenerationTask();
        projectGenerationTask2.setId("abc2");
        projectGenerationTask2.setName("testing task 2");
        projectGenerationTask2.setProgress(50);
        projectGenerationTask2.setParameters(params);
        projectGenerationTask2.setTaskType(TaskType.COUNTER);

        List<ProjectGenerationTask> TaskList = List.of(projectGenerationTask1, projectGenerationTask2);

        when(projectGenerationTaskRepository.findAll()).thenReturn(TaskList);

        List<ProjectGenerationTask> result = taskService.listTasks();
        assertEquals(TaskList, result);
        assertEquals(2, result.size());

        verify(projectGenerationTaskRepository).findAll();
    }

    @Test
    void createTask() {
        Map<String, String> params = new HashMap<>();
        params.put("x", "1");
        params.put("y", "100");

        ProjectGenerationTask projectGenerationTask = new ProjectGenerationTask();
        projectGenerationTask.setName("testing task 1");
        projectGenerationTask.setTaskType(TaskType.COUNTER);
        projectGenerationTask.setParameters(params);

        when(taskValidatorRegistry.getTaskValidator(TaskType.COUNTER)).thenReturn(taskValidator);
        when(projectGenerationTaskRepository.save(projectGenerationTask)).thenReturn(projectGenerationTask);

        ProjectGenerationTask result = taskService.createTask(projectGenerationTask);

        assertEquals(TaskType.COUNTER, result.getTaskType());
        assertEquals("testing task 1", result.getName());
        assertEquals(0, result.getProgress());
        assertEquals(TaskStatus.PENDING, result.getTaskStatus());
        assertNotNull(result.getCreationDate());

        verify(taskValidator).validate(projectGenerationTask);
        verify(projectGenerationTaskRepository).save(projectGenerationTask);
        verify(taskValidatorRegistry).getTaskValidator(TaskType.COUNTER);
    }

    @Test
    void getTask() {
        ProjectGenerationTask projectGenerationTask = new ProjectGenerationTask();
        projectGenerationTask.setId("getTaskTest1");
        projectGenerationTask.setName("get testing task 1");
        projectGenerationTask.setTaskType(TaskType.STORE_FILE);
        projectGenerationTask.setTaskStatus(TaskStatus.PENDING);

        when(projectGenerationTaskRepository.findById(projectGenerationTask.getId())).thenReturn(Optional.of(projectGenerationTask));
        ProjectGenerationTask result = taskService.getTask(projectGenerationTask.getId());

        assertEquals(TaskType.STORE_FILE, result.getTaskType());
        assertEquals("get testing task 1", result.getName());
        assertEquals("getTaskTest1", result.getId());

        verify(projectGenerationTaskRepository).findById(projectGenerationTask.getId());

    }

    @Test
    void update() {
        ProjectGenerationTask existingTask = new ProjectGenerationTask();
        existingTask.setId("updateTaskTest1");
        existingTask.setName("existing testing task 1");
        existingTask.setTaskType(TaskType.COUNTER);
        existingTask.setTaskStatus(TaskStatus.PENDING);

        ProjectGenerationTask newTask = new ProjectGenerationTask();
        newTask.setName("updated testing task 1");
        newTask.setTaskType(TaskType.COUNTER);

        when(projectGenerationTaskRepository.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));
        when(taskValidatorRegistry.getTaskValidator(TaskType.COUNTER)).thenReturn(taskValidator);
        when(projectGenerationTaskRepository.save(existingTask)).thenReturn(existingTask);

        ProjectGenerationTask updatedTask = taskService.update("updateTaskTest1", newTask);

        assertEquals(TaskType.COUNTER, updatedTask.getTaskType());
        assertEquals("updated testing task 1", updatedTask.getName());
        verify(projectGenerationTaskRepository).save(existingTask);
        verify(taskValidatorRegistry).getTaskValidator(TaskType.COUNTER);

    }

    @Test
    void delete() {
        ProjectGenerationTask existingTask = new ProjectGenerationTask();
        existingTask.setId("deleteTaskTest1");
        existingTask.setName("delete testing task 1");
        existingTask.setTaskType(TaskType.COUNTER);
        existingTask.setTaskStatus(TaskStatus.PENDING);

        taskService.delete("deleteTaskTest1");
        verify(projectGenerationTaskRepository).deleteById(existingTask.getId());
    }

    @Test
    void executeTask() {
        ProjectGenerationTask projectGenerationTask = new ProjectGenerationTask();
        projectGenerationTask.setId("executeTaskTest1");
        projectGenerationTask.setName("execute testing task 1");
        projectGenerationTask.setTaskType(TaskType.COUNTER);
        projectGenerationTask.setTaskStatus(TaskStatus.PENDING);

        when(projectGenerationTaskRepository.findById("executeTaskTest1")).thenReturn(Optional.of(projectGenerationTask));
        when(taskExecutorRegistry.getTaskExecutor(TaskType.COUNTER)).thenReturn(taskExecutor);
        taskService.executeTask("executeTaskTest1");
        verify(taskExecutor).execute(projectGenerationTask);
    }

    @Test
    void cancelTask() {
        ProjectGenerationTask projectGenerationTask = new ProjectGenerationTask();
        projectGenerationTask.setId("cancelTaskTest1");
        projectGenerationTask.setName("cancel testing task 1");
        projectGenerationTask.setTaskType(TaskType.COUNTER);
        projectGenerationTask.setTaskStatus(TaskStatus.RUNNING);

        when(projectGenerationTaskRepository.findById("cancelTaskTest1")).thenReturn(Optional.of(projectGenerationTask));
        when(taskExecutorRegistry.getTaskExecutor(TaskType.COUNTER)).thenReturn(taskExecutor);
        ProjectGenerationTask result = taskService.cancelTask("cancelTaskTest1");
        assertEquals(TaskType.COUNTER, result.getTaskType());
        assertEquals("cancel testing task 1", result.getName());
        assertEquals(TaskStatus.CANCELED, result.getTaskStatus());

        verify(projectGenerationTaskRepository).save(projectGenerationTask);
    }

    @Test
    void recoverTask() {
        ProjectGenerationTask projectGenerationTask1 = new ProjectGenerationTask();
        projectGenerationTask1.setTaskStatus(TaskStatus.RUNNING);
        projectGenerationTask1.setId("recoverTaskTest1");

        ProjectGenerationTask projectGenerationTask2 = new ProjectGenerationTask();
        projectGenerationTask1.setTaskStatus(TaskStatus.RUNNING);
        projectGenerationTask1.setId("recoverTaskTest2");

        when(projectGenerationTaskRepository.findByTaskStatus(TaskStatus.RUNNING)).thenReturn(List.of(projectGenerationTask1, projectGenerationTask2));

        taskService.recoverTask();

        assertEquals(TaskStatus.FAILED, projectGenerationTask1.getTaskStatus());
        assertEquals(TaskStatus.FAILED, projectGenerationTask2.getTaskStatus());

        verify(projectGenerationTaskRepository).saveAll(List.of(projectGenerationTask1, projectGenerationTask2));
    }
}