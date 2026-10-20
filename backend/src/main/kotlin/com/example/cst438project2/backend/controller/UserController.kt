package com.example.cst438project2.backend.controller

import com.example.cst438project2.backend.entity.User
import com.example.cst438project2.backend.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

data class CurrentUserResponse(
    val id: Long,
    val username: String?,
    val admin: Boolean,
    val googleSubject: String,
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
        @AuthenticationPrincipal jwt: Jwt?
    ): CurrentUserResponse {
        if (jwt == null || jwt.subject.isNullOrBlank()) {
            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authentication required"
            )
        }

        val user = userRepository.findByOauthSubject(jwt.subject)
            ?: userRepository.saveAndFlush(
                User(
                    username = "google_${jwt.subject}",
                    oauthSubject = jwt.subject,
                    isAdmin = false
                )
            )

        return CurrentUserResponse(
            id = requireNotNull(user.id),
            username = user.username,
            admin = user.isAdmin,
            googleSubject = jwt.subject,
            email = jwt.getClaimAsString("email")
        )
    }
}