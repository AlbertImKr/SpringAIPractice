package com.albert.springaipractice.config

import org.springframework.ai.anthropic.AnthropicChatModel
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AiConfig {

    @Bean
    fun openAiGPT4OMini(openAiModel: OpenAiChatModel, chatMemory: ChatMemory, vectorStore: VectorStore): ChatClient {
        return ChatClient.builder(
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
            .defaultAdvisors(
                MessageChatMemoryAdvisor.builder(chatMemory).build(),
                QuestionAnswerAdvisor.builder(vectorStore).build(),
                VectorStoreChatMemoryAdvisor.builder(vectorStore).build(),
                SimpleLoggerAdvisor(),
            )
            .build()
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
