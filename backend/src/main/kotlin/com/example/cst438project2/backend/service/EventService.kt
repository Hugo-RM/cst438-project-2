package com.example.cst438project2.backend.service

import com.example.cst438project2.backend.dto.EventPatchRequest
import com.example.cst438project2.backend.dto.EventRequest
import com.example.cst438project2.backend.dto.EventResponse
import com.example.cst438project2.backend.dto.toResponse
import com.example.cst438project2.backend.entity.Event
import com.example.cst438project2.backend.entity.User
import com.example.cst438project2.backend.repository.CategoryRepository
import com.example.cst438project2.backend.repository.EventRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class EventService(
    private val eventRepository: EventRepository,
    private val categoryRepository: CategoryRepository
) {
    fun getAll(
        pageable: Pageable,
        category: String?,
        from: LocalDateTime?
    ): Page<EventResponse> {
        return eventRepository.findPublished(
            category = category
                ?.trim()
                ?.takeIf { it.isNotEmpty() },
            fromTime = from,
            pageable = pageable
        ).map { it.toResponse() }
    }

    @Transactional
    fun create(
        request: EventRequest,
        user: User
    ): EventResponse {
        validateTimes(request)

        val category = categoryRepository
            .findById(request.categoryId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Category not found"
                )
            }

        val event = Event(
            title = request.title.trim(),
            description = request.description?.trim() ?: "",
            startsAt = request.startsAt,
            endsAt = request.endsAt,
            location = request.location.trim(),
            capacity = request.capacity,
            status = normalizeStatus(request.status),
            organizer = user,
            category = category
        )

        return eventRepository
            .save(event)
            .toResponse()
    }

    @Transactional
    fun update(
        id: Long,
        request: EventRequest,
        user: User
    ): EventResponse {
        validateTimes(request)

        val event = findEntity(id)
        requireOrganizer(event, user)

        val category = categoryRepository
            .findById(request.categoryId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Category not found"
                )
            }

        event.title = request.title.trim()
        event.description = request.description?.trim() ?: ""
        event.startsAt = request.startsAt
        event.endsAt = request.endsAt
        event.location = request.location.trim()
        event.capacity = request.capacity
        event.status = normalizeStatus(request.status)
        event.category = category

        return eventRepository
            .save(event)
            .toResponse()
    }

    @Transactional
    fun patch(
        id: Long,
        request: EventPatchRequest,
        user: User
    ): EventResponse {
        val event = findEntity(id)
        requireOrganizer(event, user)

        request.title?.let {
            if (it.isBlank()) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "title cannot be blank"
                )
            }

            event.title = it.trim()
        }

        request.description?.let {
            event.description = it.trim()
        }

        request.startsAt?.let {
            event.startsAt = it
        }

        request.endsAt?.let {
            event.endsAt = it
        }

        request.location?.let {
            if (it.isBlank()) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "location cannot be blank"
                )
            }

            event.location = it.trim()
        }

        request.capacity?.let {
            if (it < 1) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "capacity must be at least 1"
                )
            }

            event.capacity = it
        }

        request.status?.let {
            event.status = normalizeStatus(it)
        }

        request.categoryId?.let { categoryId ->
            event.category = categoryRepository
                .findById(categoryId)
                .orElseThrow {
                    ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found"
                    )
                }
        }

        validateEventTimes(event)

        return eventRepository
            .save(event)
            .toResponse()
    }

    @Transactional
    fun delete(
        id: Long,
        user: User
    ) {
        val event = findEntity(id)
        requireOrganizer(event, user)
        eventRepository.delete(event)
    }

    @Transactional
    fun adminDelete(
        id: Long,
        user: User
    ) {
        if (!user.isAdmin) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Administrator access required"
            )
        }

        eventRepository.delete(findEntity(id))
    }

    fun getForOrganizer(user: User): List<EventResponse> {
        return eventRepository
            .findByOrganizerId(requireNotNull(user.id))
            .map { it.toResponse() }
    }

    private fun findEntity(id: Long): Event {
        return eventRepository
            .findById(id)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Event not found"
                )
            }
    }

    private fun requireOrganizer(
        event: Event,
        user: User
    ) {
        if (event.organizer?.id != user.id) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Only the organizer can modify this event"
            )
        }
    }

    private fun validateTimes(request: EventRequest) {
        if (!request.endsAt.isAfter(request.startsAt)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "endsAt must be after startsAt"
            )
        }
    }

    private fun validateEventTimes(event: Event) {
        val startsAt = event.startsAt
        val endsAt = event.endsAt

        if (
            startsAt != null &&
            endsAt != null &&
            !endsAt.isAfter(startsAt)
        ) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "endsAt must be after startsAt"
            )
        }
    }

    private fun normalizeStatus(status: String): String {
        val normalized = status.trim().uppercase()

        if (normalized !in setOf(
                "DRAFT",
                "PUBLISHED",
                "CANCELLED"
            )
        ) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid event status"
            )
        }

        return normalized
    }

    fun getById(id: Long): EventResponse {
        return eventRepository
            .findById(id)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Event not found"
                )
            }
            .toResponse()
    }
}