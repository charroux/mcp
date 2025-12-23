package com.example.mcpserver.mcp;

import com.example.mcpserver.model.Task;
import com.example.mcpserver.service.TaskAiService;
import com.example.mcpserver.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.spec.McpSchema;
import org.springframework.ai.mcp.spring.McpTool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Outils MCP avancés avec intégration AI
 * Ces outils utilisent Spring AI pour des fonctionnalités intelligentes
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TaskAiTools {

    private final TaskService taskService;
    private final TaskAiService taskAiService;

    @McpTool(
        name = "analyze_task_sentiment",
        description = "Analyze the sentiment of a task's description using AI"
    )
    public String analyzeTaskSentiment(
            @McpSchema(description = "ID of the task to analyze", required = true) Long taskId
    ) {
        try {
            return taskService.getTaskById(taskId)
                    .map(task -> {
                        String sentiment = taskAiService.analyzeSentiment(task.getDescription());
                        return String.format(
                            "🤖 AI Sentiment Analysis for Task #%d\n\n" +
                            "Task: %s\n" +
                            "Sentiment: %s\n\n" +
                            "Description analyzed: %s",
                            taskId,
                            task.getTitle(),
                            getSentimentEmoji(sentiment) + " " + sentiment,
                            task.getDescription() != null ? task.getDescription() : "No description"
                        );
                    })
                    .orElse("❌ Task not found with ID: " + taskId);
        } catch (Exception e) {
            log.error("Error analyzing task sentiment", e);
            return "❌ Error analyzing sentiment: " + e.getMessage();
        }
    }

    @McpTool(
        name = "suggest_task_priority",
        description = "Use AI to suggest an appropriate priority level for a task"
    )
    public String suggestTaskPriority(
            @McpSchema(description = "ID of the task", required = true) Long taskId
    ) {
        try {
            return taskService.getTaskById(taskId)
                    .map(task -> {
                        String suggestedPriority = taskAiService.suggestPriority(
                                task.getTitle(),
                                task.getDescription()
                        );
                        
                        return String.format(
                            "🤖 AI Priority Suggestion for Task #%d\n\n" +
                            "Task: %s\n" +
                            "Current Priority: %s\n" +
                            "Suggested Priority: %s\n\n" +
                            "Would you like to update the priority?",
                            taskId,
                            task.getTitle(),
                            task.getPriority(),
                            suggestedPriority
                        );
                    })
                    .orElse("❌ Task not found with ID: " + taskId);
        } catch (Exception e) {
            log.error("Error suggesting task priority", e);
            return "❌ Error suggesting priority: " + e.getMessage();
        }
    }

    @McpTool(
        name = "generate_task_summary",
        description = "Generate an AI-powered concise summary of a task"
    )
    public String generateTaskSummary(
            @McpSchema(description = "ID of the task", required = true) Long taskId
    ) {
        try {
            return taskService.getTaskById(taskId)
                    .map(task -> {
                        String summary = taskAiService.generateTaskSummary(
                                task.getTitle(),
                                task.getDescription()
                        );
                        
                        return String.format(
                            "🤖 AI-Generated Summary for Task #%d\n\n" +
                            "Task: %s\n\n" +
                            "📝 Summary:\n%s",
                            taskId,
                            task.getTitle(),
                            summary
                        );
                    })
                    .orElse("❌ Task not found with ID: " + taskId);
        } catch (Exception e) {
            log.error("Error generating task summary", e);
            return "❌ Error generating summary: " + e.getMessage();
        }
    }

    @McpTool(
        name = "suggest_task_tags",
        description = "Use AI to suggest relevant tags for better task organization"
    )
    public String suggestTaskTags(
            @McpSchema(description = "ID of the task", required = true) Long taskId
    ) {
        try {
            return taskService.getTaskById(taskId)
                    .map(task -> {
                        String suggestedTags = taskAiService.suggestTags(
                                task.getTitle(),
                                task.getDescription()
                        );
                        
                        return String.format(
                            "🤖 AI Tag Suggestions for Task #%d\n\n" +
                            "Task: %s\n" +
                            "Current Tags: %s\n" +
                            "Suggested Tags: %s\n\n" +
                            "Use update_task tool to apply these tags.",
                            taskId,
                            task.getTitle(),
                            task.getTags() != null ? task.getTags() : "None",
                            suggestedTags
                        );
                    })
                    .orElse("❌ Task not found with ID: " + taskId);
        } catch (Exception e) {
            log.error("Error suggesting task tags", e);
            return "❌ Error suggesting tags: " + e.getMessage();
        }
    }

    @McpTool(
        name = "detect_task_risks",
        description = "Use AI to detect potential risks or blockers in a task"
    )
    public String detectTaskRisks(
            @McpSchema(description = "ID of the task", required = true) Long taskId
    ) {
        try {
            return taskService.getTaskById(taskId)
                    .map(task -> {
                        long daysOpen = ChronoUnit.DAYS.between(
                                task.getCreatedAt(),
                                LocalDateTime.now()
                        );
                        
                        String riskAssessment = taskAiService.detectTaskRisks(
                                task.getTitle(),
                                task.getDescription(),
                                task.getStatus().toString(),
                                (int) daysOpen
                        );
                        
                        return String.format(
                            "🤖 AI Risk Assessment for Task #%d\n\n" +
                            "Task: %s\n" +
                            "Status: %s\n" +
                            "Days Open: %d\n\n" +
                            "⚠️ Risk Assessment:\n%s",
                            taskId,
                            task.getTitle(),
                            task.getStatus(),
                            daysOpen,
                            riskAssessment
                        );
                    })
                    .orElse("❌ Task not found with ID: " + taskId);
        } catch (Exception e) {
            log.error("Error detecting task risks", e);
            return "❌ Error detecting risks: " + e.getMessage();
        }
    }

    @McpTool(
        name = "smart_create_task",
        description = "Create a task with AI-powered auto-suggestions for priority, tags, and summary"
    )
    public String smartCreateTask(
            @McpSchema(description = "Title of the task", required = true) String title,
            @McpSchema(description = "Detailed description of the task", required = true) String description
    ) {
        try {
            // Get AI suggestions
            String suggestedPriority = taskAiService.suggestPriority(title, description);
            String suggestedTags = taskAiService.suggestTags(title, description);
            String sentiment = taskAiService.analyzeSentiment(description);
            
            // Create task with AI suggestions
            Task task = new Task();
            task.setTitle(title);
            task.setDescription(description);
            
            try {
                task.setPriority(Task.TaskPriority.valueOf(suggestedPriority));
            } catch (IllegalArgumentException e) {
                task.setPriority(Task.TaskPriority.MEDIUM);
            }
            
            task.setStatus(Task.TaskStatus.TODO);
            task.setTags(suggestedTags);
            
            Task createdTask = taskService.createTask(task);
            
            return String.format(
                "✅ Smart Task Created with AI Assistance!\n\n" +
                "📌 Task #%d: %s\n\n" +
                "🤖 AI Analysis:\n" +
                "- Suggested Priority: %s (applied)\n" +
                "- Sentiment: %s %s\n" +
                "- Auto-Tags: %s\n\n" +
                "Status: %s",
                createdTask.getId(),
                createdTask.getTitle(),
                createdTask.getPriority(),
                getSentimentEmoji(sentiment),
                sentiment,
                suggestedTags,
                createdTask.getStatus()
            );
        } catch (Exception e) {
            log.error("Error creating smart task", e);
            return "❌ Error creating smart task: " + e.getMessage();
        }
    }

    private String getSentimentEmoji(String sentiment) {
        return switch (sentiment.toUpperCase()) {
            case "POSITIVE" -> "😊";
            case "NEGATIVE" -> "😟";
            case "NEUTRAL" -> "😐";
            default -> "🤔";
        };
    }
}
