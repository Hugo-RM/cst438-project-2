package com.example.cst438project2.backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "events")
class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var title: String = "",

    var description: String = "",

    var startsAt: LocalDateTime? = null,

    var endsAt: LocalDateTime? = null,

    var location: String = "",

    var capacity: Int? = null,

    var status: String = "ACTIVE",

    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    var organizer: User? = null,

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category? = null
)