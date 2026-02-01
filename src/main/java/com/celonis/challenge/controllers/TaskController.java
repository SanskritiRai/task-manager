package com.celonis.challenge.controllers;

import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.services.FileService;
import com.celonis.challenge.services.TaskService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Logger log = LogManager.getLogger(TaskController.class);

    private final TaskService taskService;
    private final FileService fileService;

    public TaskController(TaskService taskService, FileService fileService) {
        this.taskService = taskService;
        this.fileService = fileService;
    }

    @GetMapping("/")
    public List<ProjectGenerationTask> listTasks() {
        return taskService.listTasks();
    }

    @PostMapping("/")
    public ProjectGenerationTask createTask(@RequestBody @Valid ProjectGenerationTask projectGenerationTask) {
        return taskService.createTask(projectGenerationTask);
    }

    @GetMapping("/{taskId}")
    public ProjectGenerationTask getTask(@PathVariable String taskId) {
        return taskService.getTask(taskId);
    }

    @PutMapping("/{taskId}")
    public ProjectGenerationTask updateTask(@PathVariable String taskId,
                                            @RequestBody @Valid ProjectGenerationTask projectGenerationTask) {
        return taskService.update(taskId, projectGenerationTask);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable String taskId) {
        taskService.delete(taskId);
    }

    @PostMapping("/{taskId}/execute")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void executeTask(@PathVariable String taskId) {
        taskService.executeTask(taskId);
    }

    @GetMapping("/{taskId}/result")
    public ResponseEntity<FileSystemResource> getResult(@PathVariable String taskId) {
        log.info("Request received : getting result {}", taskId);
        ProjectGenerationTask projectGenerationTask = taskService.getTask(taskId);
        return fileService.getTaskResult(projectGenerationTask);
    }

    @PostMapping("/{taskId}/cancel")
    public ProjectGenerationTask cancelTask(@PathVariable String taskId) {
        log.info("Request received : canceling task {}", taskId);
        ProjectGenerationTask projectGenerationTask = taskService.getTask(taskId);
        return taskService.cancelTask(projectGenerationTask.getId());
    }

    @GetMapping("/{taskId}/progress")
    public int getProgress(@PathVariable String taskId) {
        log.info("Request received : getting progress {}", taskId);
        ProjectGenerationTask projectGenerationTask = taskService.getTask(taskId);
        return taskService.getTask(projectGenerationTask.getId()).getProgress();
    }
}
