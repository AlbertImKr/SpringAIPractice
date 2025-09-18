package com.albert.springaipractice.controller

import com.albert.springaipractice.service.ChatModelStreamService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class ChatModelStreamController(private val chatModelService: ChatModelStreamService) {

    @PostMapping("/api/chatstream")
    fun chatStream(@RequestBody input: UserInput): Flux<String> {
        return chatModelService.generateStreamText(input.question)
    }

    @PostMapping("/api/chatstreamgpt4")
    fun chatStreamGPT4(@RequestBody input: UserInput): Flux<String> {
        return chatModelService.generateStreamTextGPT4(input.question)
    }

    @PostMapping("/api/chatstreamanthropic")
    fun chatStreamAnthropic(@RequestBody input: UserInput): Flux<String> {
        return chatModelService.generateStreamTextAnthropic(input.question)
    }
}
