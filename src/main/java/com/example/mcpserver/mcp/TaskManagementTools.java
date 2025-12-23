package com.example.mcpserver.mcp;

import com.example.mcpserver.model.Task;
import com.example.mcpserver.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.spec.McpSchema;
import org.springframework.ai.mcp.spec.ServerMcpTransport;
import org.springframework.ai.mcp.spring.McpTool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * MCP Tools for Task Management
 * Ces outils sont automatiquement exposés via le protocole MCP
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TaskManagementTools {

    private final TaskService taskService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @McpTool(
        name = "create_task",
        description = "Create a new task with title, description, priority, and optional due date"
    )
    public String createTask(
            @McpSchema(description = "Title of the task", required = true) String title,
            @McpSchema(description = "Detailed description of the task") String description,
            @McpSchema(description = "Priority: LOW, MEDIUM, HIGH, or URGENT", defaultValue = "MEDIUM") String priority,
            @McpSchema(description = "Due date in ISO format (yyyy-MM-dd'T'HH:mm:ss)") String dueDate,
            @McpSchema(description = "Comma-separated tags") String tags
    ) {
        try {
            Task task = new Task();
            task.setTitle(title);
            task.setDescription(description);
            task.setPriority(Task.TaskPriority.valueOf(priority.toUpperCase()));
            task.setStatus(Task.TaskStatus.TODO);
            
            if (dueDate != null && !dueDate.isEmpty()) {
                task.setDueDate(LocalDateTime.parse(dueDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            
            if (tags != null && !tags.isEmpty()) {
                task.setTags(tags);
            }

            Task createdTask = taskService.createTask(task);
            log.info("Task created via MCP: {}", createdTask.getId());
            
            return String.format("✅ Task created successfully!\nID: %d\nTitle: %s\nPriority: %s\nStatus: %s",
                    createdTask.getId(), createdTask.getTitle(), createdTask.getPriority(), createdTask.getStatus());
        } catch (Exception e) {
            log.error("Error creating task via MCP", e);
            return "❌ Error creating task: " + e.getMessage();
        }
    }

    @McpTool(
        name = "list_tasks",
        description = "List all tasks or filter by status/priority"
    )
    public String listTasks(
            @McpSchema(description = "Filter by status: TODO, IN_PROGRESS, DONE, or CANCELLED") String status,
            @McpSchema(description = "Filter by priority: LOW, MEDIUM, HIGH, or URGENT") String priority,
            @McpSchema(description = "Sort by priority", defaultValue = "false") boolean sortByPriority
    ) {
        try {
            List<Task> tasks;
            
            if (status != null && !status.isEmpty()) {
                tasks = taskService.getTasksByStatus(Task.TaskStatus.valueOf(status.toUpperCase()));
            } else if (priority != null && !priority.isEmpty()) {
                tasks = taskService.getTasksByPriority(Task.TaskPriority.valueOf(priority.toUpperCase()));
            } else if (sortByPriority) {
                tasks = taskService.getTasksSortedByPriority();
            } else {
                tasks = taskService.getAllTasks();
            }

            if (tasks.isEmpty()) {
                return "📋 No tasks found.";
            }

            StringBuilder result = new StringBuilder(String.format("📋 Found %d task(s):\n\n", tasks.size()));
            for (Task task : tasks) {
                result.append(formatTask(task)).append("\n---\n");
            }
            
            return result.toString();
        } catch (Exception e) {
            log.error("Error listing tasks via MCP", e);
            return "❌ Error listing tasks: " + e.getMessage();
        }
    }

    @McpTool(
        name = "update_task",
        description = "Update an existing task by ID"
    )
    public String updateTask(
            @McpSchema(description = "ID of the task to update", required = true) Long id,
            @McpSchema(description = "New title") String title,
            @McpSchema(description = "New description") String description,
            @McpSchema(description = "New status: TODO, IN_PROGRESS, DONE, or CANCELLED") String status,
            @McpSchema(description = "New priority: LOW, MEDIUM, HIGH, or URGENT") String priority,
            @McpSchema(description = "New due date in ISO format") String dueDate,
            @McpSchema(description = "New tags") String tags
    ) {
        try {
            Task taskDetails = new Task();
            if (title != null) taskDetails.setTitle(title);
            if (description != null) taskDetails.setDescription(description);
            if (status != null) taskDetails.setStatus(Task.TaskStatus.valueOf(status.toUpperCase()));
            if (priority != null) taskDetails.setPriority(Task.TaskPriority.valueOf(priority.toUpperCase()));
            if (dueDate != null && !dueDate.isEmpty()) {
                taskDetails.setDueDate(LocalDateTime.parse(dueDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            if (tags != null) taskDetails.setTags(tags);

            return taskService.updateTask(id, taskDetails)
                    .map(task -> String.format("✅ Task #%d updated successfully!\n%s", id, formatTask(task)))
                    .orElse("❌ Task not found with ID: " + id);
        } catch (Exception e) {
            log.error("Error updating task via MCP", e);
            return "❌ Error updating task: " + e.getMessage();
        }
    }

    @McpTool(
        name = "delete_task",
        description = "Delete a task by ID"
    )
    public String deleteTask(
            @McpSchema(description = "ID of the task to delete", required = true) Long id
    ) {
        try {
            boolean deleted = taskService.deleteTask(id);
            return deleted 
                ? String.format("✅ Task #%d deleted successfully!", id)
                : String.format("❌ Task not found with ID: %d", id);
        } catch (Exception e) {
            log.error("Error deleting task via MCP", e);
            return "❌ Error deleting task: " + e.getMessage();
        }
    }

    @McpTool(
        name = "search_tasks",
        description = "Search tasks by keyword in title, description, or tags"
    )
    public String searchTasks(
            @McpSchema(description = "Keyword to search for", required = true) String keyword
    ) {
        try {
            List<Task> tasks = taskService.searchTasks(keyword);
            
            if (tasks.isEmpty()) {
                return String.format("🔍 No tasks found matching '%s'", keyword);
            }

            StringBuilder result = new StringBuilder(
                String.format("🔍 Found %d task(s) matching '%s':\n\n", tasks.size(), keyword)
            );
            for (Task task : tasks) {
                result.append(formatTask(task)).append("\n---\n");
            }
            
            return result.toString();
        } catch (Exception e) {
            log.error("Error searching tasks via MCP", e);
            return "❌ Error searching tasks: " + e.getMessage();
        }
    }

    private String formatTask(Task task) {
        return String.format(
            "📌 Task #%d\n" +
            "Title: %s\n" +
            "Status: %s\n" +
            "Priority: %s\n" +
            "Description: %s\n" +
            "Tags: %s\n" +
            "Due: %s\n" +
            "Created: %s",
            task.getId(),
            task.getTitle(),
            task.getStatus(),
            task.getPriority(),
            task.getDescription() != null ? task.getDescription() : "N/A",
            task.getTags() != null ? task.getTags() : "N/A",
            task.getDueDate() != null ? task.getDueDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "N/A",
            task.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
