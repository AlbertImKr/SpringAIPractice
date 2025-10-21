package com.albert.springaipractice.controller

import com.albert.springaipractice.service.RagService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/rag")
class RagController(private val ragService: RagService) {

    @PostMapping("/basic")
    fun basicRag(@RequestBody input: UserInput): RagResponse {
        val answer = ragService.questionAnswerWithBasicRag(input.question)
        return RagResponse(
            question = input.question,
            answer = answer,
            type = "Basic RAG"
        )
    }

    @PostMapping("/advanced")
    fun advancedRag(@RequestBody input: UserInput): RagResponse {
        val answer = ragService.questionAnswerWithAdvancedRag(input.question)
        return RagResponse(
            question = input.question,
            answer = answer,
            type = "Advanced RAG (similarity: 0.7, topK: 5)"
        )
    }

    @PostMapping("/filtered")
    fun filteredRag(@RequestBody request: FilteredRagRequest): RagResponse {
        val answer = ragService.questionAnswerWithFilteredRag(request.question, request.filterExpression)
        return RagResponse(
            question = request.question,
            answer = answer,
            type = "Filtered RAG",
            filter = request.filterExpression
        )
    }

    @PostMapping("/custom-prompt")
    fun customPromptRag(@RequestBody input: UserInput): RagResponse {
        val answer = ragService.questionAnswerWithCustomPrompt(input.question)
        return RagResponse(
            question = input.question,
            answer = answer,
            type = "Custom Prompt RAG"
        )
    }
}

data class FilteredRagRequest(
    val question: String,
    val filterExpression: String?,
)

data class RagResponse(
    val question: String,
    val answer: String,
    val type: String,
    val filter: String? = null,
)
