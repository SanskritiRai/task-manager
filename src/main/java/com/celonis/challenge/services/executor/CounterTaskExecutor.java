package com.celonis.challenge.services.executor;

import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.model.ProjectGenerationTaskRepository;
import com.celonis.challenge.model.TaskStatus;
import com.celonis.challenge.model.TaskType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class CounterTaskExecutor implements TaskExecutor {

    private static final Logger log = LogManager.getLogger(CounterTaskExecutor.class);

    private final ProjectGenerationTaskRepository projectGenerationTaskRepository;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    Map<String, AtomicBoolean> canceledTasks = new ConcurrentHashMap<>();

    public CounterTaskExecutor(ProjectGenerationTaskRepository projectGenerationTaskRepository) {
        this.projectGenerationTaskRepository = projectGenerationTaskRepository;
    }

    @Override
    public TaskType getSupportedTaskType() {
        return TaskType.COUNTER;
    }

    /**
     * EXECUTE COUNTER TYPE TASK
     *
     * @param task: Project generation task
     */
    @Override
    public void execute(ProjectGenerationTask task) {
        int x = Integer.parseInt(task.getParameters().get("x"));
        int y = Integer.parseInt(task.getParameters().get("y"));

        log.info("Value of parameters x={}, y={}", x, y);

        String taskId = task.getId();

        AtomicBoolean cancelFlag = new AtomicBoolean(false);
        canceledTasks.put(task.getId(), cancelFlag);

        task.setTaskStatus(TaskStatus.RUNNING);
        task.setProgress(0);
        projectGenerationTaskRepository.save(task);

        executorService.submit(() -> {
            log.info("Async thread started for task={}, thread={} ", taskId, Thread.currentThread().getName());

            try {
                int current = x;
                int totalProgress = y - x;

                while (current <= y) {
                    if (cancelFlag.get()) {
                        log.info("Canceled task={}, thread={}", taskId, Thread.currentThread().getName());
                        ProjectGenerationTask currentTask = projectGenerationTaskRepository.findById(taskId).orElseThrow();
                        currentTask.setTaskStatus(TaskStatus.CANCELED);
                        cancelFlag.set(false);
                        return;
                    }
                    int currentProgress = (int) (((double) (current - x) / totalProgress) * 100);

                    ProjectGenerationTask currentTask = projectGenerationTaskRepository.findById(taskId).orElseThrow();
                    currentTask.setProgress(currentProgress);

                    log.info("Counter progress for task={}, thread={}, progress={}", taskId, Thread.currentThread().getName(), currentProgress);
                    projectGenerationTaskRepository.save(currentTask);

                    Thread.sleep(1000);
                    current++;
                }

                ProjectGenerationTask currentTask = projectGenerationTaskRepository.findById(taskId).orElseThrow();
                currentTask.setProgress(100);
                currentTask.setTaskStatus(TaskStatus.COMPLETED);

                log.info("Task completes successfully for task={} ", taskId);
                projectGenerationTaskRepository.save(currentTask);
            } catch (Exception ex) {
                log.error("Async execution failed for task={}", taskId);
                ProjectGenerationTask currentTask = projectGenerationTaskRepository.findById(taskId).orElseThrow();
                currentTask.setTaskStatus(TaskStatus.FAILED);
                projectGenerationTaskRepository.save(currentTask);
            }
        });

    }

    /**
     * CANCEL A RUNNING COUNTER TYPE TASK
     *
     * @param task: Project generation task
     */
    @Override
    public void cancel(ProjectGenerationTask task) {
        log.warn("Cancel request for task={}", task.getId());
        AtomicBoolean cancelFlag = canceledTasks.get(task.getId());
        if (cancelFlag != null) {
            log.info("Cancel flag set for task {}", task.getId());
            cancelFlag.set(true);
        }
    }
}
