package com.example.cst438project2.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var username: String = "",

    @Column(nullable = false)
    var isAdmin: Boolean = false,

    // Nullable for existing rows: accounts without a hash cannot use password login.
    @Column(name = "password_hash")
    var passwordHash: String? = null
)