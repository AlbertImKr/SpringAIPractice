package com.albert.springaipractice.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class RagService(
    @param:Qualifier("openAiGPT4OMini") private val chatClient: ChatClient,
    private val vectorStore: VectorStore,
) {

    fun questionAnswerWithBasicRag(question: String): String {
        return chatClient.prompt()
            .user(question)
            .advisors(QuestionAnswerAdvisor(vectorStore))
            .call()
            .content()
            ?: NO_RESPONSE
    }

    fun questionAnswerWithAdvancedRag(question: String): String {
        val searchRequest = SearchRequest.builder()
            .similarityThreshold(0.7)
            .topK(5)
            .build()

        val qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
            .searchRequest(searchRequest)
            .build()

        return chatClient.prompt()
            .user(question)
            .advisors(qaAdvisor)
            .call()
            .content() ?: NO_RESPONSE
    }

    fun questionAnswerWithFilteredRag(question: String, filterExpression: String?): String {
        return if (filterExpression != null) {
            chatClient.prompt()
                .user(question)
                .advisors { it.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, filterExpression) }
                .call()
                .content() ?: NO_RESPONSE
        } else {
            questionAnswerWithBasicRag(question)
        }
    }

    fun questionAnswerWithCustomPrompt(question: String): String {
        val customPromptTemplate = PromptTemplate.builder()
            .template(
                """
            다음 컨텍스트를 바탕으로 질문에 답변해주세요:
            
            컨텍스트:
            {question_answer_context}
            
            질문: {query}
            
            답변을 한국어로 제공하고, 컨텍스트에 없는 정보는 "제공된 문서에서 해당 정보를 찾을 수 없습니다"라고 명시해주세요.
            """
            )
            .build()

        val qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
            .promptTemplate(customPromptTemplate)
            .build()

        return chatClient.prompt()
            .user(question)
            .advisors(qaAdvisor)
            .call()
            .content() ?: NO_RESPONSE
    }

    companion object {
        const val NO_RESPONSE = "응답을 생성할 수 없습니다."
    }
}
