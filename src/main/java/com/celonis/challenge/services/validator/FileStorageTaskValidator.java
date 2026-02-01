package com.celonis.challenge.services.validator;

import com.celonis.challenge.exceptions.BadRequestException;
import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.model.TaskType;
import org.springframework.stereotype.Component;

@Component
public class FileStorageTaskValidator implements TaskValidator {

    @Override
    public TaskType getSupportedType() {
        return TaskType.STORE_FILE;
    }

    /**
     * VALIDATE FILE STORAGE TYPE TASK
     *
     * @param projectGenerationTask project generation task
     */
    @Override
    public void validate(ProjectGenerationTask projectGenerationTask) {
        if (projectGenerationTask.getParameters() != null && !projectGenerationTask.getParameters().isEmpty()) {
            throw new BadRequestException("File Storage must have no parameters");
        }
    }
}
