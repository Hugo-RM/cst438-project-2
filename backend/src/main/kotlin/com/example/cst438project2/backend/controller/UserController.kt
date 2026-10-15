package com.example.cst438project2.backend.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController {

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