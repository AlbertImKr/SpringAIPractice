package com.albert.springaipractice.controller

import com.albert.springaipractice.service.ChatModelService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ChatModelController(private val chatModelService: ChatModelService) {

    @PostMapping("/api/chat")
    fun chat(@RequestBody input: UserInput): String {
        return chatModelService.generateText(input.question)
    }

    @PostMapping("/api/chatgpt4")
    fun chatGPT4(@RequestBody input: UserInput): String {
        return chatModelService.generateTextGPT4(input.question)
    }

    @PostMapping("/api/chatanthropic")
    fun chatAnthropic(@RequestBody input: UserInput): String {
        return chatModelService.generateTextAnthropic(input.question)
    }
}
