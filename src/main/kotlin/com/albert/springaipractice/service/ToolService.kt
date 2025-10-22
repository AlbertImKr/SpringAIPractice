package com.albert.springaipractice.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.ai.tool.function.FunctionToolCallback
import org.springframework.ai.tool.method.MethodToolCallback
import org.springframework.ai.tool.support.ToolDefinitions
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.util.ReflectionUtils
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

@Service
class ToolService(
    @param:Qualifier("openAiGPT4OMini") private val openAiGPT4OMini: ChatClient,
) {
    fun useAlarmTool(durationInMinutes: Int): String {
        return openAiGPT4OMini
            .prompt("현재로 부터 $durationInMinutes 분 후에 알람을 설정해줘.")
            .tools(AlarmTools())
            .call()
            .content() ?: "No response generated."
    }

    fun useMethodToolCallBack(durationInMinutes: Int): String {
        val method = ReflectionUtils.findMethod(
            DataTimeTools::class.java,
            "getCurrentDateTime"
        )!!

        val toolCallback = MethodToolCallback.builder()
            .toolDefinition(
                ToolDefinitions.builder(method)
                    .name("getCurrentDateTime")
                    .description("현재의 날짜와 시간을 반환합니다.")
                    .build()
            )
            .toolMethod(method)
            .toolObject(DataTimeTools())
            .build()

        return openAiGPT4OMini
            .prompt("현재로 부터 $durationInMinutes 분 후에 알람을 설정해줘. 및 현재 시간을 알려줘.")
            .toolCallbacks(toolCallback)
            .call()
            .content() ?: "No response generated."
    }

    fun useFunctionToolCallBack(location: String): String {
        val conversationId = UUID.randomUUID().toString()
        val toolCallback = FunctionToolCallback.builder(
            "currentTime", LocalTimeService
        )
            .description("Get the current local time for a given location.")
            .inputType(LocalTimeRequest::class.java)
            .build()

        // val chatOptions = ToolCallingChatOptions.builder()
        //     .toolCallbacks(toolCallback)
        //     .internalToolExecutionEnabled(false) // 프레임워크 내부 도구 실행 비활성화
        //     .build()

        return openAiGPT4OMini
            .prompt("해당 위치($location)의 현재 시간을 알려줘.")
            .advisors { it.param(ChatMemory.CONVERSATION_ID, conversationId) }
            .toolCallbacks(toolCallback)
            .call()
            .content() ?: "No response generated."
    }

    fun useDynamicFunctionToolCallBack(location: String): String {
        val conversationId = UUID.randomUUID().toString()

        return openAiGPT4OMini
            .prompt("해당 위치($location)의 현재 시간을 알려줘.")
            .advisors { it.param(ChatMemory.CONVERSATION_ID, conversationId) }
            .toolNames(DataTimeTools.CURRENT_DATETIME_TOOL)
            .call()
            .content() ?: "No response generated."
    }
}

val LocalTimeService: (LocalTimeRequest) -> String = { request ->
    ZoneId.of(request.location).let {
        LocalDateTime.now(it).toString()
    }
}

data class LocalTimeRequest(
    @param:ToolParam(description = "시간을 알고 싶은 위치의 시간대입니다. 예: 'Asia/Seoul', 'America/New_York'")
    val location: String,
)
