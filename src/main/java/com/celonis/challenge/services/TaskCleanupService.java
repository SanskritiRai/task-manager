package com.celonis.challenge.services;

import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.model.ProjectGenerationTaskRepository;
import com.celonis.challenge.model.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
public class TaskCleanupService {
    private static final Logger log = LoggerFactory.getLogger(TaskCleanupService.class);

    private final ProjectGenerationTaskRepository projectGenerationTaskRepository;

    public TaskCleanupService(ProjectGenerationTaskRepository projectGenerationTaskRepository) {
        this.projectGenerationTaskRepository = projectGenerationTaskRepository;
    }

    /**
     * CLEAN UP PENDING TASKS AFTER days NO OF DAYS
     * we can also use @Scheduled(cron = "0 0 5 * * ?")
     * used fixedRate for testing purpose
     */
    @Scheduled(fixedRate = 600000)
    public void cleanUpPendingTasks() {
        int days = 20;
        Date deadline = Date.from(Instant.now().minus(days, ChronoUnit.DAYS));
        List<ProjectGenerationTask> pendingTasksList = projectGenerationTaskRepository.findByTaskStatusAndCreationDateBefore(TaskStatus.PENDING, deadline);

        if (!pendingTasksList.isEmpty()) {
            log.info("Deleting pending tasks of size={} ", pendingTasksList.size());
            projectGenerationTaskRepository.deleteAll(pendingTasksList);
        } else {
            log.debug("Clean up job found no pending tasks");
        }
    }
}
