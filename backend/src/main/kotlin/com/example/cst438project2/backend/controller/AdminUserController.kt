package com.example.cst438project2.backend.controller

import com.example.cst438project2.backend.service.AdminUserResponse
import com.example.cst438project2.backend.service.AdminUserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
class AdminUserController(
    private val adminUserService: AdminUserService
) {
    @GetMapping
    fun getUsers(
        @AuthenticationPrincipal jwt: Jwt?
    ): List<AdminUserResponse> {
        return adminUserService.getUsers(jwt)
    }

    @DeleteMapping("/{userId}")
    fun deleteUser(
        @AuthenticationPrincipal jwt: Jwt?,
        @PathVariable("userId") userId: Long
    ): ResponseEntity<Void> {
        adminUserService.deleteUser(jwt, userId)
        return ResponseEntity.noContent().build()
    }
}
