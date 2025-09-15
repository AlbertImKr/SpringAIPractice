package com.albert.springaipractice.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class ChatModelService(
    @Qualifier("openAiGPT4OMini") private val openAiGPT4OMini: ChatClient,
    @Qualifier("openAiGPT4") private val chatClientGPT4: ChatClient,
    @Qualifier("authropicChatClient") private val anthropicChatClient: ChatClient
) {

    private val NO_RESPONSE = "No response generated."

    fun generateText(question: String): String {
        return openAiGPT4OMini.prompt()
            .user(question)
            .call()
            .content() ?: NO_RESPONSE
    }

    fun generateTextGPT4(question: String): String {
        return chatClientGPT4.prompt()
            .user(question)
            .call()
            .content() ?: NO_RESPONSE
    }

    fun generateTextAnthropic(question: String): String {
        return anthropicChatClient.prompt()
            .user(question)
            .call()
            .content() ?: NO_RESPONSE
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

    fun generateStreamTextAnthropic(question: String): Flux<String> {
        return anthropicChatClient.prompt()
            .user(question)
            .stream()
            .content()
    }
}
