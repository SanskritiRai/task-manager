package com.celonis.challenge.services;

import com.celonis.challenge.exceptions.BadRequestException;
import com.celonis.challenge.exceptions.ConflictException;
import com.celonis.challenge.exceptions.NotFoundException;
import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.model.ProjectGenerationTaskRepository;
import com.celonis.challenge.model.TaskStatus;
import com.celonis.challenge.services.executor.TaskExecutor;
import com.celonis.challenge.services.executor.TaskExecutorRegistry;
import com.celonis.challenge.services.validator.TaskValidatorRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private static final Logger log = LogManager.getLogger(TaskService.class);

    private final ProjectGenerationTaskRepository projectGenerationTaskRepository;

    private final TaskExecutorRegistry taskExecutorRegistry;

    private final TaskValidatorRegistry taskValidatorRegistry;

    public TaskService(ProjectGenerationTaskRepository projectGenerationTaskRepository,
                       TaskExecutorRegistry taskExecutorRegistry, TaskValidatorRegistry taskValidatorRegistry) {
        this.projectGenerationTaskRepository = projectGenerationTaskRepository;
        this.taskExecutorRegistry = taskExecutorRegistry;
        this.taskValidatorRegistry = taskValidatorRegistry;
    }

    /**
     * FETCH LIST OF ALL TASKS
     *
     * @return List<ProjectGenerationTask>
     */
    public List<ProjectGenerationTask> listTasks() {
        log.info("Listing all tasks");
        return projectGenerationTaskRepository.findAll();
    }

    /**
     * CREATE A TASK
     *
     * @param projectGenerationTask: Project generation task
     * @return ProjectGenerationTask
     */
    public ProjectGenerationTask createTask(ProjectGenerationTask projectGenerationTask) {
        log.info("Creating task with name={}, type={}", projectGenerationTask.getName(), projectGenerationTask.getTaskType());

        validateCreateTask(projectGenerationTask);

        projectGenerationTask.setId(null);
        projectGenerationTask.setCreationDate(new Date());
        projectGenerationTask.setTaskStatus(TaskStatus.PENDING);
        projectGenerationTask.setProgress(0);
        return projectGenerationTaskRepository.save(projectGenerationTask);
    }

    /**
     * RETRIEVE A TASK
     *
     * @param taskId: Task ID
     * @return ProjectGenerationTask
     */
    private ProjectGenerationTask get(String taskId) {
        Optional<ProjectGenerationTask> projectGenerationTask = projectGenerationTaskRepository.findById(taskId);
        return projectGenerationTask.orElseThrow(NotFoundException::new);
    }

    /**
     * GET A TASK
     *
     * @param taskId: Task ID
     * @return ProjectGenerationTask
     */
    public ProjectGenerationTask getTask(String taskId) {
        log.info("Getting task with id={}", taskId);
        return get(taskId);
    }

    /**
     * UPDATE AN EXISTING TASK
     *
     * @param taskId:                Task ID
     * @param projectGenerationTask: Project generation task
     * @return ProjectGenerationTask
     */
    public ProjectGenerationTask update(String taskId, ProjectGenerationTask projectGenerationTask) {
        log.info("Updating task with id={}", taskId);

        ProjectGenerationTask existingTask = projectGenerationTaskRepository.findById(taskId).orElseThrow(() -> new ConflictException("Task not found"));

        validateUpdateTask(existingTask, projectGenerationTask);

        existingTask.setName(projectGenerationTask.getName());
        existingTask.setParameters(projectGenerationTask.getParameters());

        return projectGenerationTaskRepository.save(existingTask);
    }

    /**
     * VALIDATE CREATE TASK
     *
     * @param projectGenerationTask: Project generation task
     */
    private void validateCreateTask(ProjectGenerationTask projectGenerationTask) {
        taskValidatorRegistry.getTaskValidator(projectGenerationTask.getTaskType()).validate(projectGenerationTask);
    }

    /**
     * VALIDATE UPDATE TASK
     *
     * @param existingTask: Project generation task
     * @param newTask:      Project generation task
     */
    private void validateUpdateTask(ProjectGenerationTask existingTask, ProjectGenerationTask newTask) {
        if (existingTask.getTaskType() != newTask.getTaskType()) {
            throw new ConflictException("Task type cannot be changed once created");
        }

        if (existingTask.getTaskStatus() != TaskStatus.PENDING) {
            throw new ConflictException("Only pending tasks can be updated");
        }

        taskValidatorRegistry.getTaskValidator(newTask.getTaskType()).validate(newTask);
    }

    /**
     * DELETE A TASK
     *
     * @param taskId: Task ID
     */
    public void delete(String taskId) {
        log.info("Deleting task with id={}", taskId);
        projectGenerationTaskRepository.deleteById(taskId);
    }

    /**
     * EXECUTE A TASK
     *
     * @param taskId: Task ID
     */
    public void executeTask(String taskId) {

        log.info("Executing task with id={}", taskId);

        ProjectGenerationTask projectGenerationTask = getTask(taskId);

        if (projectGenerationTask.getTaskStatus() == TaskStatus.RUNNING) {
            log.warn("Task is already running");
            throw new IllegalStateException("Already running task");
        }

        if (projectGenerationTask.getTaskStatus() == TaskStatus.COMPLETED) {
            log.warn("Task is already completed");
            throw new IllegalStateException("Already completed task");
        }

        TaskExecutor taskExecutor = taskExecutorRegistry.getTaskExecutor(projectGenerationTask.getTaskType());
        log.info("Resolved executor {}", taskExecutor.getClass().getName());

        taskExecutor.execute(projectGenerationTask);
    }

    /**
     * CANCEL A TASK
     *
     * @param taskId: Task ID
     * @return ProjectGenerationTask
     */
    public ProjectGenerationTask cancelTask(String taskId) {
        log.warn("Canceling task with id={}", taskId);

        ProjectGenerationTask projectGenerationTask = getTask(taskId);

        if (projectGenerationTask.getTaskStatus() == TaskStatus.COMPLETED) {
            log.warn("Task already completed");
            throw new BadRequestException("Already completed task, cannot cancel");
        }

        if (projectGenerationTask.getTaskStatus() == TaskStatus.CANCELED) {
            log.warn("Task is already cancelled");
            throw new BadRequestException("Already canceled task, cannot cancel");
        }

        if (projectGenerationTask.getTaskStatus() == TaskStatus.FAILED) {
            log.warn("Task is already failed");
            throw new IllegalStateException("Already failed task, cannot cancel");
        }

        TaskExecutor taskExecutor = taskExecutorRegistry.getTaskExecutor(projectGenerationTask.getTaskType());
        taskExecutor.cancel(projectGenerationTask);

        projectGenerationTask.setTaskStatus(TaskStatus.CANCELED);
        projectGenerationTaskRepository.save(projectGenerationTask);

        log.info("Task with id={} has been cancelled", taskId);
        return projectGenerationTask;
    }

    /**
     * RECOVER TASKS AFTER APPLICATION RESTART
     */
    @EventListener(ApplicationReadyEvent.class)
    public void recoverTask() {
        List<ProjectGenerationTask> runningTasksList = projectGenerationTaskRepository.findByTaskStatus(TaskStatus.RUNNING);
        if (!runningTasksList.isEmpty()) {
            log.warn("Updating {} tasks after restart", runningTasksList.size());
            runningTasksList.forEach(task -> task.setTaskStatus(TaskStatus.COMPLETED));
            projectGenerationTaskRepository.saveAll(runningTasksList);
        }
    }
}
