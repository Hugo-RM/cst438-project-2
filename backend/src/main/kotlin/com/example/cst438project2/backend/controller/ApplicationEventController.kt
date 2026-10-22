package com.example.cst438project2.backend.controller

import com.example.cst438project2.backend.service.CurrentUserService
import com.example.cst438project2.backend.service.EventService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminEventController(
    private val eventService: EventService,
    private val currentUserService: CurrentUserService
) {
    @DeleteMapping("/api/v1/admin/events/{id}")
    fun deleteAnyEvent(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        val user = currentUserService.getOrCreate(authentication)
        currentUserService.requireAdmin(user)
        eventService.adminDelete(id, user)
        return ResponseEntity.noContent().build()
    }
}
