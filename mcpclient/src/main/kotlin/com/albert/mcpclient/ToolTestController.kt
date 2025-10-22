package com.albert.mcpclient

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class ToolTestController(
    private val toolTestService: ToolTestService,
) {

    // MCP Client 목록 확인
    @GetMapping("/mcp/clients")
    fun getMcpClients(): Flux<String> = toolTestService.listMcpClients()

    // Tool Callback 목록 확인
    @GetMapping("/mcp/tool-callbacks")
    fun getToolCallbacks(): Mono<String> = toolTestService.listToolCallbackProviders()
}
