package com.example.cst438project2.backend.controller

import com.example.cst438project2.backend.dto.EventResponse
import com.example.cst438project2.backend.service.CurrentUserService
import com.example.cst438project2.backend.service.EventService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController {

    @GetMapping("/me/events")
    fun myEvents(
        authentication: Authentication,
        currentUserService: CurrentUserService,
        eventService: EventService
    ): List<EventResponse> = eventService.getForOrganizer(currentUserService.getOrCreate(authentication))

    @GetMapping("/me")
    fun currentUser(
        @AuthenticationPrincipal jwt: Jwt
    ): Map<String, String?> {
        return mapOf(
            "googleSubject" to jwt.subject,
            "email" to jwt.getClaimAsString("email")
        )
    }
}
