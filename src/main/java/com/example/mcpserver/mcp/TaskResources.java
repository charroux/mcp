package com.example.mcpserver.mcp;

import com.example.mcpserver.model.Task;
import com.example.mcpserver.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.spring.McpResource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP Resources pour exposer les tâches
 * Les resources permettent aux clients MCP d'accéder directement aux données
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TaskResources {

    private final TaskService taskService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @McpResource(
        uri = "task://all",
        name = "All Tasks",
        description = "Returns all tasks in the system",
        mimeType = "application/json"
    )
    public String getAllTasksResource() {
        try {
            List<Task> tasks = taskService.getAllTasks();
            return objectMapper.writeValueAsString(tasks);
        } catch (Exception e) {
            log.error("Error retrieving all tasks resource", e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    @McpResource(
        uri = "task://{id}",
        name = "Task by ID",
        description = "Returns a specific task by its ID",
        mimeType = "application/json"
    )
    public String getTaskByIdResource(Long id) {
        try {
            return taskService.getTaskById(id)
                    .map(task -> {
                        try {
                            return objectMapper.writeValueAsString(task);
                        } catch (Exception e) {
                            return "{\"error\": \"" + e.getMessage() + "\"}";
                        }
                    })
                    .orElse("{\"error\": \"Task not found\"}");
        } catch (Exception e) {
            log.error("Error retrieving task resource by ID: {}", id, e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    @McpResource(
        uri = "task://status/{status}",
        name = "Tasks by Status",
        description = "Returns all tasks filtered by status (TODO, IN_PROGRESS, DONE, CANCELLED)",
        mimeType = "application/json"
    )
    public String getTasksByStatusResource(String status) {
        try {
            List<Task> tasks = taskService.getTasksByStatus(Task.TaskStatus.valueOf(status.toUpperCase()));
            return objectMapper.writeValueAsString(tasks);
        } catch (Exception e) {
            log.error("Error retrieving tasks by status: {}", status, e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    @McpResource(
        uri = "task://priority/{priority}",
        name = "Tasks by Priority",
        description = "Returns all tasks filtered by priority (LOW, MEDIUM, HIGH, URGENT)",
        mimeType = "application/json"
    )
    public String getTasksByPriorityResource(String priority) {
        try {
            List<Task> tasks = taskService.getTasksByPriority(Task.TaskPriority.valueOf(priority.toUpperCase()));
            return objectMapper.writeValueAsString(tasks);
        } catch (Exception e) {
            log.error("Error retrieving tasks by priority: {}", priority, e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    @McpResource(
        uri = "task://summary",
        name = "Tasks Summary",
        description = "Returns a summary of all tasks grouped by status",
        mimeType = "text/plain"
    )
    public String getTasksSummaryResource() {
        try {
            List<Task> allTasks = taskService.getAllTasks();
            
            long todoCount = allTasks.stream()
                    .filter(t -> t.getStatus() == Task.TaskStatus.TODO)
                    .count();
            long inProgressCount = allTasks.stream()
                    .filter(t -> t.getStatus() == Task.TaskStatus.IN_PROGRESS)
                    .count();
            long doneCount = allTasks.stream()
                    .filter(t -> t.getStatus() == Task.TaskStatus.DONE)
                    .count();
            long cancelledCount = allTasks.stream()
                    .filter(t -> t.getStatus() == Task.TaskStatus.CANCELLED)
                    .count();

            return String.format(
                "📊 Task Management Summary\n\n" +
                "Total Tasks: %d\n\n" +
                "By Status:\n" +
                "  📝 TODO: %d\n" +
                "  ⚡ IN_PROGRESS: %d\n" +
                "  ✅ DONE: %d\n" +
                "  ❌ CANCELLED: %d\n",
                allTasks.size(), todoCount, inProgressCount, doneCount, cancelledCount
            );
        } catch (Exception e) {
            log.error("Error generating tasks summary", e);
            return "Error: " + e.getMessage();
        }
    }
}
