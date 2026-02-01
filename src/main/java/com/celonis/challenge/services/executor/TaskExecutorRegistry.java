package com.celonis.challenge.services.executor;

import com.celonis.challenge.model.TaskType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TaskExecutorRegistry {

    private final Map<TaskType, TaskExecutor> executorsMap = new HashMap<>();

    public TaskExecutorRegistry(List<TaskExecutor> taskExecutors) {
        for (TaskExecutor taskExecutor : taskExecutors) {
            executorsMap.put(taskExecutor.getSupportedTaskType(), taskExecutor);
        }
    }

    /**
     * FETCH TASK EXECUTOR
     *
     * @param taskType: Task type
     * @return TaskExecutor
     */
    public TaskExecutor getTaskExecutor(TaskType taskType) {
        TaskExecutor executor = executorsMap.get(taskType);
        if (executor == null) {
            throw new IllegalStateException("No executor registered for type " + taskType);
        }
        return executor;
    }
}
