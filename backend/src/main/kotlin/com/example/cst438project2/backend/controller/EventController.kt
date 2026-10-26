package com.example.cst438project2.backend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import com.example.cst438project2.backend.dto.EventRequest
import com.example.cst438project2.backend.dto.EventPatchRequest
import com.example.cst438project2.backend.dto.EventResponse
import com.example.cst438project2.backend.service.CurrentUserService
import com.example.cst438project2.backend.service.EventService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/events")
class EventController(
    private val eventService: EventService,
    private val currentUserService: CurrentUserService
) {
    @Operation(summary = "List published events (paginated, filterable)")
    @ApiResponse(responseCode = "200", description = "One page of published events")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @GetMapping
    fun getEvents(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) from: LocalDateTime?,
        @RequestParam(defaultValue = "startsAt") sort: String
    ): Page<EventResponse> {
        val safePage = page.coerceAtLeast(0)
        val safeSize = size.coerceIn(1, 100)
        val pageable = PageRequest.of(safePage, safeSize, Sort.by(sort).ascending())
        return eventService.getAll(pageable, category, from)
    }

    @Operation(summary = "Get one event")
    @ApiResponse(responseCode = "200", description = "The event")
    @ApiResponse(responseCode = "404", description = "Not found")
    @GetMapping("/{id}")
    fun getEvent(@PathVariable id: Long): EventResponse =
        eventService.getById(id)

    @Operation(summary = "Create an event")
    @ApiResponse(responseCode = "201", description = "Created")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    @PostMapping
    fun createEvent(
        @Valid @RequestBody request: EventRequest,
        authentication: Authentication
    ): ResponseEntity<EventResponse> = ResponseEntity.status(201).body(
        eventService.create(request, currentUserService.getOrCreate(authentication))
    )

    @Operation(summary = "Replace my event (organizer only)")
    @ApiResponse(responseCode = "200", description = "The replaced event")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Not allowed")
    @ApiResponse(responseCode = "404", description = "Not found")
    @PutMapping("/{id}")
    fun updateEvent(
        @PathVariable id: Long,
        @Valid @RequestBody request: EventRequest,
        authentication: Authentication
    ): EventResponse = eventService.update(
        id, request, currentUserService.getOrCreate(authentication)
    )

    @Operation(summary = "Change some fields of my event (organizer only)")
    @ApiResponse(responseCode = "200", description = "The updated event")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Not allowed")
    @ApiResponse(responseCode = "404", description = "Not found")
    @PatchMapping("/{id}")
    fun patchEvent(
        @PathVariable id: Long,
        @RequestBody request: EventPatchRequest,
        authentication: Authentication
    ): EventResponse = eventService.patch(
        id, request, currentUserService.getOrCreate(authentication)
    )

    @Operation(summary = "Delete my event (organizer only)")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Not allowed")
    @ApiResponse(responseCode = "404", description = "Not found")
    @DeleteMapping("/{id}")
    fun deleteEvent(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        eventService.delete(id, currentUserService.getOrCreate(authentication))
        return ResponseEntity.noContent().build()
    }

}
