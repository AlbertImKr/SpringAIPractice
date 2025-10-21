package com.albert.springaipractice.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class ChatModelService(
    @param:Qualifier("openAiGPT4OMini") private val openAiGPT4OMini: ChatClient,
    @param:Qualifier("openAiGPT4") private val chatClientGPT4: ChatClient,
    @param:Qualifier("authropicChatClient") private val anthropicChatClient: ChatClient,
) {
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

    companion object {
        const val NO_RESPONSE = "No response generated."
    }
}
