package com.albert.springaipractice.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class ChatModelService(@Qualifier("openAiGPT4OMini") private val openAiGPT4OMini: ChatClient,
    @Qualifier("openAiGPT4") private val chatClientGPT4: ChatClient) {

    fun generateText(question: String): String {
        return openAiGPT4OMini.prompt()
            .user(question)
            .call()
            .content() ?: "No response generated."
    }

    fun generateTextGPT4(question: String): String {
        return chatClientGPT4.prompt()
            .user(question)
            .call()
            .content() ?: "No response generated."
    }

    fun generateStreamText(question: String): Flux<String> {
        return openAiGPT4OMini.prompt()
            .user(question)
            .stream()
            .content()
    }

    fun generateStreamTextGPT4(question: String): Flux<String> {
        return chatClientGPT4.prompt()
            .user(question)
            .stream()
            .content()
    }
}
