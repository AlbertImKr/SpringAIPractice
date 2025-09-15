package com.albert.springaipractice.config

import org.springframework.ai.anthropic.AnthropicChatModel
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AiConfig {

    @Bean
    fun openAiGPT4OMini(openAiModel: OpenAiChatModel): ChatClient {
        return ChatClient.create(
            openAiModel.mutate()
                .defaultOptions(
                    OpenAiChatOptions.builder()
                        .model("gpt-4o-mini")
                        .temperature(0.3)
                        .maxTokens(1000)
                        .build()
                )
                .build()
        )
    }

    @Bean
    fun openAiGPT4(openAiModel: OpenAiChatModel): ChatClient {
        return ChatClient.create(
            openAiModel.mutate()
                .defaultOptions(
                    OpenAiChatOptions.builder()
                        .model("gpt-4")
                        .temperature(0.3)
                        .maxTokens(1000)
                        .build()
                )
                .build()
        )
    }

    @Bean
    fun authropicChatClient(authropicChatModel: AnthropicChatModel): ChatClient {
        return ChatClient.create(authropicChatModel)
    }
}
