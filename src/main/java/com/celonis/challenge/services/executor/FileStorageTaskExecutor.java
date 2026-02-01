package com.celonis.challenge.services.executor;

import com.celonis.challenge.exceptions.InternalException;
import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.model.ProjectGenerationTaskRepository;
import com.celonis.challenge.model.TaskStatus;
import com.celonis.challenge.model.TaskType;
import com.celonis.challenge.services.FileService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
public class FileStorageTaskExecutor implements TaskExecutor {

    private static final Logger log = LogManager.getLogger(FileStorageTaskExecutor.class);

    FileService fileService;
    ProjectGenerationTaskRepository projectGenerationTaskRepository;

    public FileStorageTaskExecutor(FileService fileService, ProjectGenerationTaskRepository projectGenerationTaskRepository) {
        this.fileService = fileService;
        this.projectGenerationTaskRepository = projectGenerationTaskRepository;
    }

    @Override
    public TaskType getSupportedTaskType() {
        return TaskType.STORE_FILE;
    }

    /**
     * EXECUTE A FILE STORAGE TYPE TASK
     *
     * @param task: Project generation task
     */
    @Override
    public void execute(ProjectGenerationTask task) {
        log.info("Starting task file storage task={} execution", task);
        try {
            task.setTaskStatus(TaskStatus.RUNNING);
            task.setProgress(0);
            projectGenerationTaskRepository.save(task);

            URL url = Thread.currentThread().getContextClassLoader().getResource("FileTest.rtf");
            if (url == null) {
                throw new InternalException("File not found in resources: FileTest.rtf");
            }

            fileService.storeResult(task, url);

            task.setProgress(100);
            task.setTaskStatus(TaskStatus.COMPLETED);
            projectGenerationTaskRepository.save(task);
            log.info("Task file store task={} execution complete", task);
        } catch (Exception e) {
            log.error("Error executing task file store task={} execution", task, e);
            task.setTaskStatus(TaskStatus.FAILED);
            projectGenerationTaskRepository.save(task);
            throw new InternalException(e);
        }
    }

    /**
     * CANCEL A FILE STORAGE TYPE TASK TASK
     *
     * @param task: Project generation task
     */
    @Override
    public void cancel(ProjectGenerationTask task) {
        log.warn("Request received for cancel task file store task={}", task);
        if (!task.getTaskStatus().equals(TaskStatus.COMPLETED)) {
            task.setTaskStatus(TaskStatus.CANCELED);
            projectGenerationTaskRepository.save(task);
        }
    }
}
