package com.example.cst438project2.backend

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {
    @GetMapping("/api/v1/health")
    fun health(): Map<String, String> {
        return mapOf("status" to "ok")
    }
}