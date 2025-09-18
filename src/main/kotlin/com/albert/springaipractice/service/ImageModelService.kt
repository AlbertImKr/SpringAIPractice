package com.albert.springaipractice.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.content.Media
import org.springframework.ai.image.Image
import org.springframework.ai.image.ImagePrompt
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiImageModel
import org.springframework.ai.openai.OpenAiImageOptions
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.MimeType
import org.springframework.util.MimeTypeUtils
import org.springframework.web.multipart.MultipartFile
import java.net.URI

@Service
class ImageModelService(
    @Qualifier("openAiGPT4OMini") private val openAiGPT4OMini: ChatClient,
    private val openAiModel: OpenAiChatModel,
    private val imageModel: OpenAiImageModel,
) {
    fun analyzeImage(image: MultipartFile, question: String): String {

        val resource = InputStreamResource(image.inputStream)
        val mimeType = MimeType.valueOf(image.contentType ?: MediaType.IMAGE_JPEG_VALUE)

        return openAiGPT4OMini.prompt()
            .user { userSpec ->
                userSpec.text(question)
                    .media(mimeType, resource)
            }
            .call()
            .content() ?: "No response generated."
    }

    fun analyzeImageUrl(imageUrl: String, question: String): String {

        val systemMessage = SystemMessage("한국어로 대답해줘")
        val url = URI(imageUrl)

        val userMessage = UserMessage.builder()
            .text(question)
            .media(Media(MimeTypeUtils.IMAGE_PNG, url))
            .build()

        val prompt = Prompt(
            systemMessage,
            userMessage
        )

        return openAiModel.call(prompt).result.output.text ?: "No response generated."
    }

    fun generateImage(question: String): Image {
        val prompt = ImagePrompt(
            question,
            OpenAiImageOptions.builder()
                .height(1024)
                .width(1024)
                .build()
        )

        val result = imageModel.call(prompt).result

        return result.output
    }
}
