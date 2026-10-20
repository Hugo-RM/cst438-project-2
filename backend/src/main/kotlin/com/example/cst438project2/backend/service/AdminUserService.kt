package com.example.cst438project2.backend.service

import com.example.cst438project2.backend.repository.UserRepository
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

data class AdminUserResponse(
    val id: Long,
    val username: String?,
    val role: String
)

@Service
class AdminUserService(
    private val userRepository: UserRepository,
    private val authorization: AdminAuthorizationService,
    private val entityManager: EntityManager
) {
    @Transactional(readOnly = true)
    fun getUsers(jwt: Jwt?): List<AdminUserResponse> {
        authorization.requireAdmin(jwt)

        return userRepository.findAll(Sort.by("id")).map { user ->
            AdminUserResponse(
                id = requireNotNull(user.id),
                username = user.username,
                role = if (user.isAdmin) "ADMIN" else "USER"
            )
        }
    }

    @Transactional
    fun deleteUser(jwt: Jwt?, userId: Long) {
        val admin = authorization.requireAdmin(jwt)

        if (admin.id == userId) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "You cannot delete your own administrator account"
            )
        }

        val user = userRepository.findById(userId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
                )
            }

        // Remove RSVPs made by the user or attached to their events.
        entityManager.createQuery(
            """
            DELETE FROM Rsvp r
            WHERE r.user.id = :userId
               OR r.event.id IN (
                   SELECT e.id FROM Event e
                   WHERE e.organizer.id = :userId
               )
            """.trimIndent()
        )
            .setParameter("userId", userId)
            .executeUpdate()

        // Remove the user's organized events.
        entityManager.createQuery(
            "DELETE FROM Event e WHERE e.organizer.id = :userId"
        )
            .setParameter("userId", userId)
            .executeUpdate()

        userRepository.delete(user)
        userRepository.flush()
    }
}
