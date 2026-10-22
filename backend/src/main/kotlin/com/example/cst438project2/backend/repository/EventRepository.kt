package com.example.cst438project2.backend.repository

import com.example.cst438project2.backend.entity.Event
import java.time.LocalDateTime
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface EventRepository : JpaRepository<Event, Long> {
    fun findByOrganizerId(organizerId: Long): List<Event>

    @Query(
        """
        select e from Event e
    where e.status = 'PUBLISHED'
      and (
          cast(:category as string) is null
          or lower(e.category.name) = lower(cast(:category as string))
      )
      and (
          cast(:fromTime as timestamp) is null
          or e.startsAt >= :fromTime
      )

        """
    )
    fun findPublished(
        @Param("category") category: String?,
        @Param("fromTime") fromTime: LocalDateTime?,
        pageable: Pageable
    ): Page<Event>
}