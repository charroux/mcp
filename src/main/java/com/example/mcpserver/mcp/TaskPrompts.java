package com.example.mcpserver.mcp;

import com.example.mcpserver.model.Task;
import com.example.mcpserver.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.spec.McpSchema;
import org.springframework.ai.mcp.spring.McpPrompt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MCP Prompts pour des interactions AI avancées
 * Les prompts permettent de pré-configurer des interactions complexes
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TaskPrompts {

    private final TaskService taskService;

    @McpPrompt(
        name = "summarize_tasks",
        description = "Generate a comprehensive summary of all tasks with insights and recommendations"
    )
    public String summarizeTasks(
            @McpSchema(description = "Include detailed analysis", defaultValue = "true") boolean detailed
    ) {
        List<Task> tasks = taskService.getAllTasks();
        
        if (tasks.isEmpty()) {
            return "No tasks available to summarize.";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("# Task Management Summary\n\n");
        
        // Statistics
        long todoCount = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.TODO).count();
        long inProgressCount = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.IN_PROGRESS).count();
        long doneCount = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.DONE).count();
        long urgentCount = tasks.stream().filter(t -> t.getPriority() == Task.TaskPriority.URGENT).count();
        
        summary.append(String.format("## Overview\n"));
        summary.append(String.format("- Total Tasks: %d\n", tasks.size()));
        summary.append(String.format("- TODO: %d\n", todoCount));
        summary.append(String.format("- In Progress: %d\n", inProgressCount));
        summary.append(String.format("- Completed: %d\n", doneCount));
        summary.append(String.format("- Urgent Tasks: %d\n\n", urgentCount));
        
        if (detailed) {
            // High priority tasks
            List<Task> urgentTasks = tasks.stream()
                    .filter(t -> t.getPriority() == Task.TaskPriority.URGENT || t.getPriority() == Task.TaskPriority.HIGH)
                    .filter(t -> t.getStatus() != Task.TaskStatus.DONE)
                    .collect(Collectors.toList());
            
            if (!urgentTasks.isEmpty()) {
                summary.append("## ⚠️ High Priority Tasks Requiring Attention\n\n");
                urgentTasks.forEach(task -> {
                    summary.append(String.format("- **%s** [%s] - %s\n", 
                            task.getTitle(), task.getPriority(), task.getStatus()));
                });
                summary.append("\n");
            }
            
            // Recommendations
            summary.append("## 💡 Recommendations\n\n");
            if (urgentCount > 0) {
                summary.append("- Focus on urgent tasks first\n");
            }
            if (inProgressCount > 5) {
                summary.append("- Consider completing some in-progress tasks before starting new ones\n");
            }
            if (todoCount > 10) {
                summary.append("- Large backlog detected - prioritize and break down tasks\n");
            }
        }
        
        return summary.toString();
    }

    @McpPrompt(
        name = "suggest_next_task",
        description = "AI-powered suggestion for the next task to work on based on priority and status"
    )
    public String suggestNextTask() {
        List<Task> activeTasks = taskService.getAllTasks().stream()
                .filter(t -> t.getStatus() == Task.TaskStatus.TODO || t.getStatus() == Task.TaskStatus.IN_PROGRESS)
                .collect(Collectors.toList());
        
        if (activeTasks.isEmpty()) {
            return "🎉 Great job! No active tasks remaining. Time to create new goals or take a break!";
        }
        
        // Find highest priority task
        Task nextTask = activeTasks.stream()
                .sorted((t1, t2) -> {
                    // Sort by priority first (URGENT > HIGH > MEDIUM > LOW)
                    int priorityCompare = Integer.compare(
                            getPriorityValue(t2.getPriority()),
                            getPriorityValue(t1.getPriority())
                    );
                    if (priorityCompare != 0) return priorityCompare;
                    
                    // Then by status (IN_PROGRESS before TODO)
                    int statusCompare = Integer.compare(
                            t1.getStatus() == Task.TaskStatus.IN_PROGRESS ? 0 : 1,
                            t2.getStatus() == Task.TaskStatus.IN_PROGRESS ? 0 : 1
                    );
                    if (statusCompare != 0) return statusCompare;
                    
                    // Finally by creation date (older first)
                    return t1.getCreatedAt().compareTo(t2.getCreatedAt());
                })
                .findFirst()
                .orElse(null);
        
        if (nextTask == null) {
            return "No suitable task found.";
        }
        
        StringBuilder suggestion = new StringBuilder();
        suggestion.append("🎯 **Suggested Next Task**\n\n");
        suggestion.append(String.format("**%s**\n\n", nextTask.getTitle()));
        suggestion.append(String.format("- Priority: %s\n", getPriorityEmoji(nextTask.getPriority())));
        suggestion.append(String.format("- Status: %s\n", nextTask.getStatus()));
        
        if (nextTask.getDescription() != null && !nextTask.getDescription().isEmpty()) {
            suggestion.append(String.format("- Description: %s\n", nextTask.getDescription()));
        }
        
        if (nextTask.getDueDate() != null) {
            suggestion.append(String.format("- Due Date: %s\n", nextTask.getDueDate()));
        }
        
        suggestion.append("\n**Why this task?**\n");
        if (nextTask.getPriority() == Task.TaskPriority.URGENT) {
            suggestion.append("- This is an URGENT task that requires immediate attention\n");
        } else if (nextTask.getPriority() == Task.TaskPriority.HIGH) {
            suggestion.append("- High priority task that should be addressed soon\n");
        }
        
        if (nextTask.getStatus() == Task.TaskStatus.IN_PROGRESS) {
            suggestion.append("- Already in progress - finish what you started!\n");
        }
        
        return suggestion.toString();
    }

    @McpPrompt(
        name = "analyze_productivity",
        description = "Analyze task completion patterns and provide productivity insights"
    )
    public String analyzeProductivity() {
        List<Task> tasks = taskService.getAllTasks();
        
        if (tasks.isEmpty()) {
            return "No tasks available for productivity analysis.";
        }
        
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.DONE).count();
        long cancelledTasks = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.CANCELLED).count();
        long activeTasks = totalTasks - completedTasks - cancelledTasks;
        
        double completionRate = totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0;
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("# 📈 Productivity Analysis\n\n");
        
        analysis.append("## Task Statistics\n\n");
        analysis.append(String.format("- Total Tasks: %d\n", totalTasks));
        analysis.append(String.format("- Completed: %d (%.1f%%)\n", completedTasks, completionRate));
        analysis.append(String.format("- Cancelled: %d\n", cancelledTasks));
        analysis.append(String.format("- Active: %d\n\n", activeTasks));
        
        analysis.append("## Performance Indicators\n\n");
        
        if (completionRate >= 70) {
            analysis.append("✅ **Excellent completion rate!** You're getting things done.\n\n");
        } else if (completionRate >= 40) {
            analysis.append("⚠️ **Moderate completion rate.** Consider focusing on finishing tasks.\n\n");
        } else {
            analysis.append("❌ **Low completion rate.** May need to review task management approach.\n\n");
        }
        
        // Most used priority
        Task.TaskPriority mostUsedPriority = tasks.stream()
                .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()))
                .entrySet().stream()
                .max((e1, e2) -> Long.compare(e1.getValue(), e2.getValue()))
                .map(e -> e.getKey())
                .orElse(Task.TaskPriority.MEDIUM);
        
        analysis.append(String.format("- Most common priority level: **%s**\n", mostUsedPriority));
        
        analysis.append("\n## 💡 Insights\n\n");
        
        if (activeTasks > completedTasks * 2) {
            analysis.append("- You're creating tasks faster than completing them. Consider task breakdown or delegation.\n");
        }
        
        if (cancelledTasks > totalTasks * 0.2) {
            analysis.append("- High cancellation rate detected. Review task feasibility before creation.\n");
        }
        
        long urgentTasks = tasks.stream()
                .filter(t -> t.getPriority() == Task.TaskPriority.URGENT)
                .count();
        if (urgentTasks > totalTasks * 0.3) {
            analysis.append("- Too many urgent tasks. Consider better planning to avoid urgency.\n");
        }
        
        return analysis.toString();
    }

    @McpPrompt(
        name = "group_related_tasks",
        description = "Group tasks by common themes, tags, or keywords for better organization"
    )
    public String groupRelatedTasks(
            @McpSchema(description = "Keyword to group by (optional)") String keyword
    ) {
        List<Task> tasks = keyword != null && !keyword.isEmpty() 
                ? taskService.searchTasks(keyword)
                : taskService.getAllTasks();
        
        if (tasks.isEmpty()) {
            return "No tasks found to group.";
        }
        
        // Group by tags
        var tasksByTag = tasks.stream()
                .filter(t -> t.getTags() != null && !t.getTags().isEmpty())
                .flatMap(t -> java.util.Arrays.stream(t.getTags().split(","))
                        .map(String::trim)
                        .map(tag -> new Object[] { tag, t }))
                .collect(Collectors.groupingBy(
                        arr -> (String) arr[0],
                        Collectors.mapping(arr -> (Task) arr[1], Collectors.toList())
                ));
        
        StringBuilder result = new StringBuilder();
        result.append("# 🏷️ Tasks Grouped by Tags\n\n");
        
        if (tasksByTag.isEmpty()) {
            result.append("No tasks with tags found.\n");
        } else {
            tasksByTag.forEach((tag, tagTasks) -> {
                result.append(String.format("## %s (%d tasks)\n\n", tag, tagTasks.size()));
                tagTasks.forEach(task -> {
                    result.append(String.format("- **%s** [%s] - %s\n", 
                            task.getTitle(), task.getPriority(), task.getStatus()));
                });
                result.append("\n");
            });
        }
        
        return result.toString();
    }

    private int getPriorityValue(Task.TaskPriority priority) {
        return switch (priority) {
            case URGENT -> 4;
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }

    private String getPriorityEmoji(Task.TaskPriority priority) {
        return switch (priority) {
            case URGENT -> "🔴 URGENT";
            case HIGH -> "🟠 HIGH";
            case MEDIUM -> "🟡 MEDIUM";
            case LOW -> "🟢 LOW";
        };
    }
}
