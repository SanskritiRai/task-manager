package com.celonis.challenge.services.validator;

import com.celonis.challenge.exceptions.BadRequestException;
import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.model.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CounterTaskValidator implements TaskValidator {

    private static final Logger log = LoggerFactory.getLogger(CounterTaskValidator.class);

    @Override
    public TaskType getSupportedType() {
        return TaskType.COUNTER;
    }

    /**
     * VALIDATE COUNTER TYPE TASK
     *
     * @param projectGenerationTask Project generation task
     */
    @Override
    public void validate(ProjectGenerationTask projectGenerationTask) {
        Map<String, String> paramMap = projectGenerationTask.getParameters();

        if (paramMap == null || !paramMap.containsKey("x") || !paramMap.containsKey("y")) {
            log.error("Invalid task parameters");
            throw new BadRequestException("Missing required parameters x and y");
        }

        try {
            int x = Integer.parseInt(paramMap.get("x"));
            int y = Integer.parseInt(paramMap.get("y"));

            if (x >= y) {
                throw new BadRequestException("x should be lesser than y");
            }
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Invalid x or y value");
        }
    }
}
