package com.celonis.challenge.services.validator;

import com.celonis.challenge.model.TaskType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TaskValidatorRegistry {

    Map<TaskType, TaskValidator> validatorMap;

    public TaskValidatorRegistry(List<TaskValidator> validators) {
        this.validatorMap = validators.stream()
                .collect(Collectors.toMap(TaskValidator::getSupportedType, v -> v));
    }

    /**
     * VALIDATE COUNTER TYPE TASK
     *
     * @param taskType taskType generation task
     */

    public TaskValidator getTaskValidator(TaskType taskType) {
        TaskValidator validator = validatorMap.get(taskType);
        if (validator == null) {
            throw new IllegalStateException("No validator registered for type " + taskType);
        }
        return validator;
    }
}
