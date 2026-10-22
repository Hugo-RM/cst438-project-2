package com.example.cst438project2.backend.controller

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

    @GetMapping("/{id}")
    fun getEvent(@PathVariable id: Long): EventResponse =
        eventService.getById(id)

    @PostMapping
    fun createEvent(
        @Valid @RequestBody request: EventRequest,
        authentication: Authentication
    ): ResponseEntity<EventResponse> = ResponseEntity.status(201).body(
        eventService.create(request, currentUserService.getOrCreate(authentication))
    )

    @PutMapping("/{id}")
    fun updateEvent(
        @PathVariable id: Long,
        @Valid @RequestBody request: EventRequest,
        authentication: Authentication
    ): EventResponse = eventService.update(
        id, request, currentUserService.getOrCreate(authentication)
    )

    @PatchMapping("/{id}")
    fun patchEvent(
        @PathVariable id: Long,
        @RequestBody request: EventPatchRequest,
        authentication: Authentication
    ): EventResponse = eventService.patch(
        id, request, currentUserService.getOrCreate(authentication)
    )

    @DeleteMapping("/{id}")
    fun deleteEvent(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        eventService.delete(id, currentUserService.getOrCreate(authentication))
        return ResponseEntity.noContent().build()
    }

}
