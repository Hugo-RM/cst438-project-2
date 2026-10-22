package com.example.cst438project2.backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "rsvps")
class Rsvp(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    var event: Event? = null,

    @Column(nullable = false)
    var status: String = "GOING",

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
