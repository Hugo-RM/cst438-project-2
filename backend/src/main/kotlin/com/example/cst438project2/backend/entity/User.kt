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

    @Column(name = "oauth_subject", unique = true)
    var oauthSubject: String? = null,

    // Google "sub" claim; null for accounts created through password registration.
    @Column(name = "oauth_subject", unique = true)
    var oauthSubject: String? = null,

    @Column(nullable = false)
    var isAdmin: Boolean = false,

    // Nullable for existing rows: accounts without a hash cannot use password login.
    @Column(name = "password_hash")
    var passwordHash: String? = null
)
