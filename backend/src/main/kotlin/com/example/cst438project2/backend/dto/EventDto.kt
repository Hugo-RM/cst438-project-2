package com.example.cst438project2.backend.dto

import com.example.cst438project2.backend.entity.Event
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class EventRequest(
    @field:NotBlank val title: String,
    val description: String? = null,
    @field:NotNull val startsAt: LocalDateTime,
    @field:NotNull val endsAt: LocalDateTime,
    @field:NotBlank val location: String,
    @field:Min(1) val capacity: Int? = null,
    val status: String = "DRAFT",
    @field:NotNull val categoryId: Long
)

data class EventPatchRequest(
    val title: String? = null,
    val description: String? = null,
    val startsAt: LocalDateTime? = null,
    val endsAt: LocalDateTime? = null,
    val location: String? = null,
    val capacity: Int? = null,
    val status: String? = null,
    val categoryId: Long? = null
)

data class EventResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val startsAt: LocalDateTime?,
    val endsAt: LocalDateTime?,
    val location: String,
    val capacity: Int?,
    val status: String,
    val organizerId: Long,
    val categoryId: Long,
    val categoryName: String
)

fun Event.toResponse() = EventResponse(
    id = requireNotNull(id),
    title = title,
    description = description,
    startsAt = startsAt,
    endsAt = endsAt,
    location = location,
    capacity = capacity,
    status = status,
    organizerId = requireNotNull(organizer?.id),
    categoryId = requireNotNull(category?.id),
    categoryName = category?.name ?: ""
)
