A Spring Boot backend service for managing tasks with asynchronous execution, progress tracking, cancellation, and automatic cleanup.
This service allows users to create tasks, run them in the background, monitor progress, cancel execution, and safely handle unfinished tasks after restarts. It is designed for systems that need to run long-running jobs without blocking API requests.

**Features**
Create and manage tasks via REST APIs
Execute tasks asynchronously
Track progress (0–100)
Cancel running tasks
Handle unfinished tasks after restart
Clean up stale pending tasks automatically
Download results for file-based tasks

**Supported tasks**
Counter task: counts from x to y asynchronously with progress tracking
File task: generates a file and allows download after completion

**How it works**
Client → REST API → Task Service → Async Executor → Database

**Requirements**
Java 11+
Maven
MySQL

**Run**
mvn clean install
mvn spring-boot:run

**API endpoints**
Create task	POST /api/tasks
Execute task	POST /api/tasks/{id}/execute
Get progress	GET /api/tasks/{id}/progress
Cancel task	POST /api/tasks/{id}/cancel
Download result	GET /api/tasks/{id}/result
