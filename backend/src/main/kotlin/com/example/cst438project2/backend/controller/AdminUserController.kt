package com.example.cst438project2.backend.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
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
    @Operation(summary = "List all users (admin only)")
    @ApiResponse(responseCode = "200", description = "All users")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Not allowed")
    @GetMapping
    fun getUsers(
        @AuthenticationPrincipal jwt: Jwt?
    ): List<AdminUserResponse> {
        return adminUserService.getUsers(jwt)
    }

    @Operation(summary = "Delete a user and all their data (admin only)")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Not allowed")
    @ApiResponse(responseCode = "404", description = "Not found")
    @ApiResponse(responseCode = "409", description = "Cannot delete your own account")
    @DeleteMapping("/{userId}")
    fun deleteUser(
        @AuthenticationPrincipal jwt: Jwt?,
        @PathVariable("userId") userId: Long
    ): ResponseEntity<Void> {
        adminUserService.deleteUser(jwt, userId)
        return ResponseEntity.noContent().build()
    }
}
