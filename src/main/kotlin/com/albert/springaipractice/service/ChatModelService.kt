package com.albert.springaipractice.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class ChatModelService(private val chatClient: ChatClient) {

    fun generateText(question: String): String {
        return chatClient.prompt()
            .user(question)
            .call()
            .content() ?: "No response generated."
    }

    fun generateStreamText(question: String): Flux<String> {
        return chatClient.prompt()
            .user(question)
            .stream()
            .content()
    }
}
