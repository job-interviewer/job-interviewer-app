package com.interview.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class InterviewServerApplication

fun main(args: Array<String>) {
    runApplication<InterviewServerApplication>(*args)
}
