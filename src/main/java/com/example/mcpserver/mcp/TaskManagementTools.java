package com.example.mcpserver.mcp;

import com.example.mcpserver.model.Task;
import com.example.mcpserver.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * MCP Tools for Task Management
 * Ces méthodes représentent les outils MCP (à exposer via un protocole MCP custom)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TaskManagementTools {

    private final TaskService taskService;

    /**
     * Create a new task
     */
    public String createTask(String title, String description, String priority, String dueDate, String tags) {
        try {
            Task task = new Task();
            task.setTitle(title);
            task.setDescription(description);
            task.setPriority(Task.TaskPriority.valueOf(priority != null ? priority.toUpperCase() : "MEDIUM"));
            task.setStatus(Task.TaskStatus.TODO);
            
            if (dueDate != null && !dueDate.isEmpty()) {
                task.setDueDate(LocalDateTime.parse(dueDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            
            if (tags != null && !tags.isEmpty()) {
                task.setTags(tags);
            }

            Task createdTask = taskService.createTask(task);
            log.info("Task created: {}", createdTask.getId());
            
            return String.format("✅ Task created successfully!\nID: %d\nTitle: %s\nPriority: %s\nStatus: %s",
                    createdTask.getId(), createdTask.getTitle(), createdTask.getPriority(), createdTask.getStatus());
        } catch (Exception e) {
            log.error("Error creating task", e);
            return "❌ Error creating task: " + e.getMessage();
        }
    }

    /**
     * List all tasks
     */
    public String listTasks(String status, String priority, boolean sortByPriority) {
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
            log.error("Error listing tasks", e);
            return "❌ Error listing tasks: " + e.getMessage();
        }
    }

    /**
     * Update a task
     */
    public String updateTask(Long id, String title, String description, String status, 
                           String priority, String dueDate, String tags) {
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
            log.error("Error updating task", e);
            return "❌ Error updating task: " + e.getMessage();
        }
    }

    /**
     * Delete a task
     */
    public String deleteTask(Long id) {
        try {
            boolean deleted = taskService.deleteTask(id);
            return deleted 
                ? String.format("✅ Task #%d deleted successfully!", id)
                : String.format("❌ Task not found with ID: %d", id);
        } catch (Exception e) {
            log.error("Error deleting task", e);
            return "❌ Error deleting task: " + e.getMessage();
        }
    }

    /**
     * Search tasks by keyword
     */
    public String searchTasks(String keyword) {
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
            log.error("Error searching tasks", e);
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
