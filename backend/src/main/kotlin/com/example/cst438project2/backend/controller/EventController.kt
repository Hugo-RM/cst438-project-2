package com.example.cst438project2.backend.controller

import com.example.cst438project2.backend.entity.Event
import com.example.cst438project2.backend.service.EventService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/events")
class EventController(
    private val eventService: EventService
) {
    @GetMapping
    fun getEvents(): List<Event> =
        eventService.getAll()

    @GetMapping("/{id}")
    fun getEvent(@PathVariable id: Long): Event =
        eventService.getById(id)
}