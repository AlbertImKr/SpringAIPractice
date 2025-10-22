package com.albert.springaipractice.controller

import com.albert.springaipractice.service.ToolService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ToolController(
    private val toolService: ToolService,
) {

    @PostMapping("/api/tool/alarm")
    fun useAlarmTool(@RequestBody request: AlarmSetRequest): String {
        return toolService.useAlarmTool(request.durationInMinutes)
    }

    @PostMapping("/api/tool/method-callback")
    fun useMethodToolCallback(@RequestBody request: AlarmSetRequest): String {
        return toolService.useMethodToolCallBack(request.durationInMinutes)
    }

    @GetMapping("/api/tool/function-callback")
    fun useFunctionToolCallback(@RequestBody request: LocationRequest): String {
        return toolService.useFunctionToolCallBack(request.location)
    }

    @GetMapping("/api/tool/dynamic-tool")
    fun useDynamicToolCallback(@RequestBody request: LocationRequest): String {
        return toolService.useDynamicFunctionToolCallBack(request.location)
    }
}

data class AlarmSetRequest(val durationInMinutes: Int)

data class LocationRequest(val location: String)
