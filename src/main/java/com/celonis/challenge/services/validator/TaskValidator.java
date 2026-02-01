package com.celonis.challenge.services.validator;

import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.model.TaskType;

public interface TaskValidator {
    TaskType getSupportedType();

    void validate(ProjectGenerationTask projectGenerationTask);
}
