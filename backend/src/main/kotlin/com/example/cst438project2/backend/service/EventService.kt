package com.example.cst438project2.backend.service

import com.example.cst438project2.backend.entity.Event
import com.example.cst438project2.backend.repository.EventRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class EventService(
    private val eventRepository: EventRepository
) {
    fun getAll(): List<Event> =
        eventRepository.findAll()

    fun getById(id: Long): Event =
        eventRepository.findById(id)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Event not found"
                )
            }
}