package com.albert.springaipractice.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class ChatModelService(private val chatClient: ChatClient) {

    fun generateText(question: String): String {
        return chatClient.prompt()
            .system("사용자 질문에 대해 한국어로 답변을 해야 합니다.")
            .user(question)
            .options(
                ChatOptions.builder()
                    .model("gpt-4o-mini")
                    .temperature(0.3)
                    .maxTokens(1000)
                    .build()
            )
            .call()
            .content() ?: "No response generated."
    }

    fun generateStreamText(question: String): Flux<String> {
        return chatClient.prompt()
            .system("사용자 질문에 대해 한국어로 답변을 해야 합니다.")
            .user(question)
            .options(
                ChatOptions.builder()
                    .model("gpt-4o-mini")
                    .temperature(0.3)
                    .maxTokens(1000)
                    .build()
            )
            .stream()
            .content()
    }
}
