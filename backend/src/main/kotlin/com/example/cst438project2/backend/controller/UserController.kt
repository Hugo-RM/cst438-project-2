package com.example.cst438project2.backend.controller

import com.example.cst438project2.backend.entity.User
import com.example.cst438project2.backend.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

data class CurrentUserResponse(
    val id: Long,
    val username: String?,
    val admin: Boolean,
    val googleSubject: String?,
    val email: String?
)

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userRepository: UserRepository
) {
    @GetMapping("/me")
    @Transactional
    fun currentUser(
        authentication: Authentication?
    ): CurrentUserResponse {
        if (authentication == null || !authentication.isAuthenticated) {
            throw unauthorized()
        }

        val user: User
        val googleSubject: String?
        val email: String?

        when (authentication) {
            is JwtAuthenticationToken -> {
                val jwt = authentication.token
                val subject = jwt.subject

                if (subject.isNullOrBlank()) {
                    throw unauthorized()
                }

                user = userRepository.findByOauthSubject(subject)
                    ?: userRepository.saveAndFlush(
                        User(
                            username = "google_$subject",
                            oauthSubject = subject,
                            isAdmin = false
                        )
                    )

                googleSubject = subject
                email = jwt.getClaimAsString("email")
            }

            is UsernamePasswordAuthenticationToken -> {
                user = userRepository.findByUsername(authentication.name)
                    ?: throw unauthorized()

                googleSubject = user.oauthSubject
                email = null
            }

            else -> throw unauthorized()
        }

        return CurrentUserResponse(
            id = requireNotNull(user.id),
            username = user.username,
            admin = user.isAdmin,
            googleSubject = googleSubject,
            email = email
        )
    }

    private fun unauthorized(): ResponseStatusException {
        return ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Authentication required"
        )
    }
}