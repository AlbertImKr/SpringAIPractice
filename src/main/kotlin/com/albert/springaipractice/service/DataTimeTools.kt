package com.albert.springaipractice.service

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Description
import org.springframework.context.i18n.LocaleContextHolder
import java.time.LocalDateTime
import java.time.ZoneId

@Configuration(proxyBeanMethods = false)
class DataTimeTools {

    @Description("The current date time in local timezone")
    fun getCurrentDateTime(): String {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString()
    }

    @Bean(CURRENT_DATETIME_TOOL)
    @Description("The current date time in local timezone")
    fun getCurrentDateTimeTool(): (LocalTimeRequest) -> String = { request ->
        ZoneId.of(request.location).let {
            LocalDateTime.now(it).toString()
        }
    }

    companion object {
        const val CURRENT_DATETIME_TOOL = "CURRENT_DATETIME_TOOL"
    }
}
