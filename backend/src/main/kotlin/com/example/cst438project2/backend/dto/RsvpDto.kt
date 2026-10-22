package com.example.cst438project2.backend.dto

import com.example.cst438project2.backend.entity.Rsvp
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class RsvpRequest(
    @field:NotBlank val status: String = "GOING"
)

data class RsvpResponse(
    val id: Long,
    val userId: Long,
    val username: String,
    val eventId: Long,
    val status: String,
    val createdAt: LocalDateTime
)

fun Rsvp.toResponse() = RsvpResponse(
    id = requireNotNull(id),
    userId = requireNotNull(user?.id),
    username = user?.username ?: "",
    eventId = requireNotNull(event?.id),
    status = status,
    createdAt = createdAt
)
