package com.interview.server.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class HealthController {
    /**
     * Reports service health status.
     *
     * @return A map containing the entry "status" -> "ok".
     */
    @GetMapping("/health")
    fun health(): Map<String, String> = mapOf("status" to "ok")
}
