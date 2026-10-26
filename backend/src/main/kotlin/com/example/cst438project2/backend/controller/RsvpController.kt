package com.example.cst438project2.backend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
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
    @Operation(summary = "RSVP to an event")
    @ApiResponse(responseCode = "201", description = "RSVP recorded")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    @ApiResponse(responseCode = "404", description = "Not found")
    @ApiResponse(responseCode = "409", description = "Already RSVP'd or event is full")
    @PostMapping
    fun create(
        @PathVariable eventId: Long,
        @Valid @RequestBody request: RsvpRequest,
        authentication: Authentication
    ): ResponseEntity<RsvpResponse> = ResponseEntity.status(201).body(
        rsvpService.create(eventId, request, currentUserService.getOrCreate(authentication))
    )

    @Operation(summary = "Cancel my RSVP")
    @ApiResponse(responseCode = "204", description = "Cancelled")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    @ApiResponse(responseCode = "404", description = "Not found")
    @DeleteMapping
    fun cancel(
        @PathVariable eventId: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        rsvpService.cancel(eventId, currentUserService.getOrCreate(authentication))
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "List RSVPs for my event (organizer only)")
    @ApiResponse(responseCode = "200", description = "Every RSVP for the event")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Not allowed")
    @ApiResponse(responseCode = "404", description = "Not found")
    @GetMapping
    fun attendees(
        @PathVariable eventId: Long,
        authentication: Authentication
    ): List<RsvpResponse> = rsvpService.attendees(
        eventId, currentUserService.getOrCreate(authentication)
    )
}
