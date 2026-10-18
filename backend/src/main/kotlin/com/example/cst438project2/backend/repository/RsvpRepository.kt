package com.example.cst438project2.backend.repository

import com.example.cst438project2.backend.entity.Rsvp
import org.springframework.data.jpa.repository.JpaRepository

interface RsvpRepository : JpaRepository<Rsvp, Long> {
    fun findByUserIdAndEventId(userId: Long, eventId: Long): Rsvp?
    fun findByEventId(eventId: Long): List<Rsvp>
    fun deleteByUserIdAndEventId(userId: Long, eventId: Long)
}
