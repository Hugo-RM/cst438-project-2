package com.example.cst438project2.backend.repository

import com.example.cst438project2.backend.entity.Event
import org.springframework.data.jpa.repository.JpaRepository

interface EventRepository : JpaRepository<Event, Long>