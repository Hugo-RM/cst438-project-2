package com.example.cst438project2.backend.service

import com.example.cst438project2.backend.dto.RsvpRequest
import com.example.cst438project2.backend.dto.RsvpResponse
import com.example.cst438project2.backend.dto.toResponse
import com.example.cst438project2.backend.entity.Event
import com.example.cst438project2.backend.entity.Rsvp
import com.example.cst438project2.backend.entity.User
import com.example.cst438project2.backend.repository.EventRepository
import com.example.cst438project2.backend.repository.RsvpRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class RsvpService(
    private val rsvpRepository: RsvpRepository,
    private val eventRepository: EventRepository
) {
    @Transactional
    fun create(eventId: Long, request: RsvpRequest, user: User): RsvpResponse {
        val event = findEvent(eventId)
        if (rsvpRepository.findByUserIdAndEventId(requireNotNull(user.id), eventId) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "You already RSVP'd to this event")
        }
        val status = request.status.trim().uppercase()
        if (status !in setOf("GOING", "MAYBE", "DECLINED")) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid RSVP status")
        }
        if (status == "GOING" && event.capacity != null && goingCount(event) >= event.capacity!!) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Event is at capacity")
        }
        return rsvpRepository.save(Rsvp(user = user, event = event, status = status)).toResponse()
    }

    @Transactional
    fun cancel(eventId: Long, user: User) {
        findEvent(eventId)
        rsvpRepository.deleteByUserIdAndEventId(requireNotNull(user.id), eventId)
    }

    fun attendees(eventId: Long, user: User): List<RsvpResponse> {
        val event = findEvent(eventId)
        if (event.organizer?.id != user.id) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only the organizer can view attendees")
        }
        return rsvpRepository.findByEventId(eventId).map { it.toResponse() }
    }

    private fun findEvent(id: Long): Event = eventRepository.findById(id).orElseThrow {
        ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found")
    }

    private fun goingCount(event: Event): Long =
        rsvpRepository.findByEventId(requireNotNull(event.id)).count { it.status == "GOING" }.toLong()
}
