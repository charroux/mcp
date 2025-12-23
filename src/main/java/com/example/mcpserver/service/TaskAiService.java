package com.example.mcpserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service d'analyse AI pour les tâches
 * Utilise Spring AI pour fournir des insights intelligents
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskAiService {

    private final ChatClient.Builder chatClientBuilder;

    /**
     * Analyse le sentiment d'une description de tâche
     */
    public String analyzeSentiment(String taskDescription) {
        if (taskDescription == null || taskDescription.isEmpty()) {
            return "NEUTRAL";
        }

        try {
            String promptText = """
                Analyze the sentiment of the following task description and respond with ONLY one word:
                POSITIVE, NEGATIVE, or NEUTRAL.
                
                Task description: {description}
                
                Sentiment:
                """;

            PromptTemplate promptTemplate = new PromptTemplate(promptText);
            Prompt prompt = promptTemplate.create(Map.of("description", taskDescription));

            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt(prompt)
                    .call()
                    .content();

            log.info("Sentiment analysis result: {}", response);
            return response.trim().toUpperCase();
        } catch (Exception e) {
            log.error("Error analyzing sentiment", e);
            return "NEUTRAL";
        }
    }

    /**
     * Suggère une priorité pour une tâche basée sur sa description
     */
    public String suggestPriority(String title, String description) {
        try {
            String promptText = """
                Based on the following task information, suggest an appropriate priority level.
                Respond with ONLY one of these words: LOW, MEDIUM, HIGH, or URGENT.
                
                Consider:
                - Urgency keywords (urgent, asap, critical, immediately)
                - Impact keywords (important, essential, critical, must)
                - Time constraints mentioned
                
                Task title: {title}
                Task description: {description}
                
                Suggested priority:
                """;

            PromptTemplate promptTemplate = new PromptTemplate(promptText);
            Prompt prompt = promptTemplate.create(Map.of(
                    "title", title != null ? title : "",
                    "description", description != null ? description : ""
            ));

            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt(prompt)
                    .call()
                    .content();

            log.info("Priority suggestion result: {}", response);
            return response.trim().toUpperCase();
        } catch (Exception e) {
            log.error("Error suggesting priority", e);
            return "MEDIUM";
        }
    }

    /**
     * Génère un résumé intelligent d'une tâche
     */
    public String generateTaskSummary(String title, String description) {
        try {
            String promptText = """
                Generate a concise one-sentence summary of this task, highlighting the key action and outcome.
                Keep it under 100 characters.
                
                Task title: {title}
                Task description: {description}
                
                Summary:
                """;

            PromptTemplate promptTemplate = new PromptTemplate(promptText);
            Prompt prompt = promptTemplate.create(Map.of(
                    "title", title != null ? title : "",
                    "description", description != null ? description : ""
            ));

            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt(prompt)
                    .call()
                    .content();

            log.info("Generated task summary: {}", response);
            return response.trim();
        } catch (Exception e) {
            log.error("Error generating task summary", e);
            return "Unable to generate summary";
        }
    }

    /**
     * Suggère des tags pertinents pour une tâche
     */
    public String suggestTags(String title, String description) {
        try {
            String promptText = """
                Based on the task information, suggest 2-4 relevant tags (keywords) that categorize this task.
                Respond with comma-separated tags only, no explanation.
                Examples: backend, frontend, bug, feature, documentation, testing
                
                Task title: {title}
                Task description: {description}
                
                Tags:
                """;

            PromptTemplate promptTemplate = new PromptTemplate(promptText);
            Prompt prompt = promptTemplate.create(Map.of(
                    "title", title != null ? title : "",
                    "description", description != null ? description : ""
            ));

            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt(prompt)
                    .call()
                    .content();

            log.info("Suggested tags: {}", response);
            return response.trim();
        } catch (Exception e) {
            log.error("Error suggesting tags", e);
            return "";
        }
    }

    /**
     * Détecte si une tâche est bloquée ou à risque
     */
    public String detectTaskRisks(String title, String description, String status, int daysOpen) {
        try {
            String promptText = """
                Analyze this task for potential risks or blockers.
                Respond with a brief risk assessment (2-3 sentences) or "No significant risks detected".
                
                Task title: {title}
                Description: {description}
                Status: {status}
                Days open: {daysOpen}
                
                Risk assessment:
                """;

            PromptTemplate promptTemplate = new PromptTemplate(promptText);
            Prompt prompt = promptTemplate.create(Map.of(
                    "title", title != null ? title : "",
                    "description", description != null ? description : "",
                    "status", status,
                    "daysOpen", String.valueOf(daysOpen)
            ));

            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient.prompt(prompt)
                    .call()
                    .content();

            log.info("Risk assessment: {}", response);
            return response.trim();
        } catch (Exception e) {
            log.error("Error detecting task risks", e);
            return "Unable to assess risks";
        }
    }
}
