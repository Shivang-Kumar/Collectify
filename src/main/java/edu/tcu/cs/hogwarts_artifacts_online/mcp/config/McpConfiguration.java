package edu.tcu.cs.hogwarts_artifacts_online.mcp.config;


import org.springframework.context.annotation.Configuration;

import edu.tcu.cs.hogwarts_artifacts_online.mcp.tools.HealthTool;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;

@Configuration
public class McpConfiguration {

    @Bean
    ToolCallbackProvider toolCallbackProvider(HealthTool healthTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(healthTool)
                .build();
    }
}