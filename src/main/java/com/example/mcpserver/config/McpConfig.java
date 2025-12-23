package com.example.mcpserver.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.spring.McpSyncServerConfiguration;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration MCP et Spring AI
 * Spring AI détecte automatiquement les @McpTool, @McpResource, @McpPrompt
 */
@Configuration
public class McpConfig {

    @Bean
    public McpSyncServerConfiguration mcpServerConfiguration() {
        return McpSyncServerConfiguration.builder()
                .serverInfo(info -> info
                        .name("task-management-server")
                        .version("2.0.0")
                        .description("AI-powered task management with MCP"))
                .build();
    }

    @Bean
    public ChatClient.Builder chatClientBuilder(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }
}

