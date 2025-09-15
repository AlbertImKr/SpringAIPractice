package com.albert.springaipractice.service

import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class ChatModelService(private val chatModel: ChatModel) {

    fun generateText(question: String): String {
        val systemMessage = SystemMessage.builder()
            .text("사용자 질문에 대해 한국어로 답변을 해야 합니다.")
            .build()

        val userMessage = UserMessage.builder()
            .text(question)
            .build()

        val chatOptions = ChatOptions.builder()
            .model("gpt-4o-mini")
            .temperature(0.3)
            .maxTokens(1000)
            .build()

        val prompt = Prompt.builder()
            .messages(listOf(systemMessage, userMessage))
            .chatOptions(chatOptions)
            .build()

        return chatModel.call(prompt).result.output.text ?: "No response generated."
    }

    fun generateStreamText(question: String): Flux<String> {
        val systemMessage = SystemMessage.builder()
            .text("사용자 질문에 대해 한국어로 답변을 해야 합니다.")
            .build()

        val userMessage = UserMessage.builder()
            .text(question)
            .build()

        val chatOptions = ChatOptions.builder()
            .model("gpt-4o-mini")
            .temperature(0.3)
            .maxTokens(1000)
            .build()

        val prompt = Prompt.builder()
            .messages(listOf(systemMessage, userMessage))
            .chatOptions(chatOptions)
            .build()

        val stringBuilder = StringBuilder()
        return chatModel.stream(prompt).map { chatResponse ->
            val assistantMessage = chatResponse.result.output
            val text = assistantMessage.text ?: ""
            stringBuilder.append(text)
            text
        }
    }
}
