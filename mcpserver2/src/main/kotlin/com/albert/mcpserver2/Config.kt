package com.albert.mcpserver2

import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Config {

    @Bean
    fun toolCallbackProvider(toolService: ToolService): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(toolService)
            .build()
    }
}
