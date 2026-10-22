package com.example.cst438project2.backend.service

import com.example.cst438project2.backend.entity.User
import com.example.cst438project2.backend.repository.UserRepository
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus


@Service
class CurrentUserService(private val userRepository: UserRepository) {
    @Transactional
    fun getOrCreate(authentication: Authentication): User {
        val jwt = authentication.principal as? Jwt
            ?: throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid authentication token"
            )

        if (jwt.getClaimAsString("auth_type") == "password") {
            val userId = jwt.subject.toLongOrNull()
                ?: throw ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid user ID in token"
                )

            return userRepository.findById(userId).orElseThrow {
                ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User account not found"
                )
            }
        }


        val subject = jwt.subject
        val email = jwt.getClaimAsString("email")

        val existing = userRepository.findByOauthSubject(subject)
            ?: email?.let { userRepository.findByUsername(it) }

        if (existing != null) {
            if (existing.oauthSubject == null) {
                existing.oauthSubject = subject
                return userRepository.save(existing)
            }
            return existing
        }

        val username = email ?: subject
        return userRepository.save(
            User(username = username, oauthSubject = subject)
        )
    }
    fun requireAdmin(user: User) {
        if (!user.isAdmin) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Admin access required"
            )
        }
    }
}
