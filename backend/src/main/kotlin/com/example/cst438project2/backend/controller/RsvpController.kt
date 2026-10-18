package com.example.cst438project2.backend.controller

import com.example.cst438project2.backend.dto.RsvpRequest
import com.example.cst438project2.backend.dto.RsvpResponse
import com.example.cst438project2.backend.service.CurrentUserService
import com.example.cst438project2.backend.service.RsvpService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/events/{eventId}/rsvps")
class RsvpController(
    private val rsvpService: RsvpService,
    private val currentUserService: CurrentUserService
) {
    @PostMapping
    fun create(
        @PathVariable eventId: Long,
        @Valid @RequestBody request: RsvpRequest,
        authentication: Authentication
    ): ResponseEntity<RsvpResponse> = ResponseEntity.status(201).body(
        rsvpService.create(eventId, request, currentUserService.getOrCreate(authentication))
    )

    @DeleteMapping
    fun cancel(
        @PathVariable eventId: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        rsvpService.cancel(eventId, currentUserService.getOrCreate(authentication))
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun attendees(
        @PathVariable eventId: Long,
        authentication: Authentication
    ): List<RsvpResponse> = rsvpService.attendees(
        eventId, currentUserService.getOrCreate(authentication)
    )
}
