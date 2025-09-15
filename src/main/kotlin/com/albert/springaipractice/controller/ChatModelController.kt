package com.albert.springaipractice.controller

import com.albert.springaipractice.service.ChatModelService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class ChatModelController(private val chatModelService: ChatModelService) {

    @PostMapping(
        value = ["/api/chat"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun chat(@RequestParam question: String): String {
        return chatModelService.generateText(question)
    }

    @PostMapping(
        value = ["/api/chatgpt4"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun chatGPT4(@RequestParam question: String): String {
        return chatModelService.generateTextGPT4(question)
    }

    @PostMapping(
        value = ["/api/chatstream"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun chatStream(@RequestParam question: String): Flux<String> {
        return chatModelService.generateStreamText(question)
    }


    @PostMapping(
        value = ["/api/chatstreamgpt4"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun chatStreamGPT4(@RequestParam question: String): Flux<String> {
        return chatModelService.generateStreamTextGPT4(question)
    }
}
