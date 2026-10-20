package com.example.cst438project2.backend.entity



import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    var username: String? = null,

    @Column(nullable = false)
    var isAdmin: Boolean = false,

    @Column(name = "password_hash")
    var passwordHash: String? = null,

    @Column(name = "oauth_subject", unique = true)
    var oauthSubject: String? = null
)