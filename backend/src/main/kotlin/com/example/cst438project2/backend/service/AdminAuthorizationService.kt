package com.example.cst438project2.backend.service

import com.example.cst438project2.backend.entity.User
import com.example.cst438project2.backend.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AdminAuthorizationService(
    private val userRepository: UserRepository
) {
    fun requireAdmin(jwt: Jwt?): User {
        if (jwt == null || jwt.subject.isNullOrBlank()) {
            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authentication required"
            )
        }

        val user = userRepository.findByOauthSubject(jwt.subject)
            ?: throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "No linked user account"
            )

        if (!user.isAdmin) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Administrator permission required"
            )
        }

        return user
    }
}