package com.albert.mcpserver1

import org.springframework.ai.tool.annotation.Tool
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

@Service
class ToolService {

    @Tool(description = "현재 날짜와 시간을 반환합니다.")
    fun getCurrentDateTime(): String {
        return LocalDateTime.now()
            .atZone(LocaleContextHolder.getTimeZone().toZoneId())
            .toString()
    }

    @Tool(description = "주어진 시간 후에 알람 설정을 합니다. 시간을 'YYYY-MM-DDTHH:MM' 형식으로 입력하세요.")
    fun setAlarm(dateTime: String): String {
        val alarmTime = LocalDateTime.parse(dateTime, ISO_LOCAL_DATE_TIME)
        println("알람이 설정되었습니다: $alarmTime")
        return "알람이 $dateTime 에 설정되었습니다."
    }
}
