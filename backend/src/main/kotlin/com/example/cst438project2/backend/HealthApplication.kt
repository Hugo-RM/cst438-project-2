package com.example.cst438project2.backend

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {
    @Operation(summary = "Liveness check")
    @ApiResponse(responseCode = "200", description = "The API is up")
    @GetMapping("/api/v1/health")
    fun health(): Map<String, String> {
        return mapOf("status" to "ok")
    }
}