package com.albert.mcpserver2

import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Service

@Service
class ToolService {

    @Tool(description = "level 해당하는 점수를 반환합니다. (1~10 사이의 정수)")
    fun getLevelScore(level: Int): Int {
        return when (level) {
            in 1..10 -> level * 10
            else -> throw IllegalArgumentException("Level must be between 1 and 10")
        }
    }
}
