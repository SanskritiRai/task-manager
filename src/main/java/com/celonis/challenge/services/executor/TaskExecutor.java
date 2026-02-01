package com.celonis.challenge.services.executor;

import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.model.TaskType;

public interface TaskExecutor {

    /**
     * Supported task type
     *
     * @return TaskType
     */
    TaskType getSupportedTaskType();

    /**
     * Executes a task
     *
     * @param task: Project Generation Task
     */
    void execute(ProjectGenerationTask task);

    /**
     * Cancels a task
     *
     * @param task: Project Generation Task
     */
    void cancel(ProjectGenerationTask task);
}
