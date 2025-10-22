package com.albert.mcpclient

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class McpclientApplication

fun main(args: Array<String>) {
    runApplication<McpclientApplication>(*args)
}
