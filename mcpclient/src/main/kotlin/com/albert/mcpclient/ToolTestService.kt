package com.albert.mcpclient

import io.modelcontextprotocol.client.McpSyncClient
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class ToolTestService(
    val clients: List<McpSyncClient>,
    val toolCallbackProvider: SyncMcpToolCallbackProvider,
) {

    fun listMcpClients(): Flux<String> {
        return Flux.fromIterable(clients)
            .map { client ->
                "MCP Sync Client: ${client.clientInfo}"
            }
            .doOnNext { println(it) }
    }

    fun listToolCallbackProviders(): Mono<String> {
        return Mono.fromCallable {
            println("Tool Callback Provider: $toolCallbackProvider")
            val toolCallbacks = toolCallbackProvider.toolCallbacks

            val toolCallbackDescriptions = toolCallbacks.joinToString(separator = "\n") { toolCallback ->
                "- Tool Callback: ${toolCallback.toolMetadata} (${toolCallback.toolDefinition})"
            }
            "Registered Tool Callbacks:\n$toolCallbackDescriptions"
        }.subscribeOn(Schedulers.boundedElastic())
    }
}

