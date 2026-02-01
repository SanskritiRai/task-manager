package com.celonis.challenge.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ProjectGenerationTaskRepository extends JpaRepository<ProjectGenerationTask, String> {
    List<ProjectGenerationTask> findByTaskStatus(TaskStatus taskStatus);

    List<ProjectGenerationTask> findByTaskStatusAndCreationDateBefore(TaskStatus taskStatus, Date creationDateBefore);
}
