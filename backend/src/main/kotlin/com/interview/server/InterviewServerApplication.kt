package com.interview.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class InterviewServerApplication

/**
 * Application entry point that starts the Spring Boot application.
 *
 * @param args Command-line arguments forwarded to the application. 
 */
fun main(args: Array<String>) {
    runApplication<InterviewServerApplication>(*args)
}
