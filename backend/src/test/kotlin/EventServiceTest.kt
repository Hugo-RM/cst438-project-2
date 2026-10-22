
package com.example.cst438project2.backend.service

import com.example.cst438project2.backend.dto.EventRequest
import com.example.cst438project2.backend.entity.Category
import com.example.cst438project2.backend.entity.Event
import com.example.cst438project2.backend.entity.User
import com.example.cst438project2.backend.repository.CategoryRepository
import com.example.cst438project2.backend.repository.EventRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.Optional

class EventServiceTest {

    private val eventRepository =
        Mockito.mock(EventRepository::class.java)

    private val categoryRepository =
        Mockito.mock(CategoryRepository::class.java)

    private val eventService =
        EventService(eventRepository, categoryRepository)

    @Test
    fun `create saves event with correct details`() {
        val category = Category(id = 1L, name = "Academic")
        val user = User(id = 10L, username = "testuser")

        val startTime = LocalDateTime.of(2026, 10, 25, 14, 0)
        val endTime = LocalDateTime.of(2026, 10, 25, 16, 0)

        val request = EventRequest(
            title = "  CS Club Meetup  ",
            description = "  Weekly club meeting  ",
            startsAt = startTime,
            endsAt = endTime,
            location = "  Library  ",
            categoryId = 1L
        )

        Mockito.`when`(categoryRepository.findById(1L))
            .thenReturn(Optional.of(category))

        Mockito.`when`(
            eventRepository.save(Mockito.any(Event::class.java))
        ).thenAnswer { invocation ->
            val savedEvent = invocation.getArgument<Event>(0)
            savedEvent.id = 100L
            savedEvent
        }

        // Act: call the method being tested
        val result = eventService.create(request, user)

        // Assert: check the returned event
        assertEquals(100L, result.id)
        assertEquals("CS Club Meetup", result.title)
        assertEquals("Weekly club meeting", result.description)
        assertEquals("Library", result.location)
        assertEquals(startTime, result.startsAt)
        assertEquals(endTime, result.endsAt)
        assertEquals(10L, result.organizerId)
        assertEquals(1L, result.categoryId)
        assertEquals("Academic", result.categoryName)

        Mockito.verify(eventRepository)
            .save(Mockito.any(Event::class.java))
    }
}
