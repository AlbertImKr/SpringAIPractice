package com.albert.springaipractice.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AiConfig {

    @Bean
    fun openAiGPT4OMini(chatClientBuilder: ChatClient.Builder): ChatClient {
        return chatClientBuilder
            .defaultSystem("사용자 질문에 대해 한국어로 답변을 해야 합니다.")
            .defaultOptions(
                ChatOptions.builder()
                    .model("gpt-4o-mini")
                    .temperature(0.3)
                    .maxTokens(1000)
                    .build()
            )
            .build()
    }

    @Bean
    fun openAiGPT4(chatClientBuilder: ChatClient.Builder): ChatClient {
        return chatClientBuilder
            .defaultSystem("사용자 질문에 대해 한국어로 답변을 해야 합니다.")
            .defaultOptions(
                ChatOptions.builder()
                    .model("gpt-4")
                    .temperature(0.3)
                    .maxTokens(1000)
                    .build()
            )
            .build()
    }
}
