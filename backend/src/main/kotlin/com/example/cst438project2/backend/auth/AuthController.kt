package com.example.cst438project2.backend.auth

import com.example.cst438project2.backend.entity.User
import com.example.cst438project2.backend.repository.UserRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

/** Credentials sent over HTTPS; never log or persist this request. */
class Credentials(val username: String = "", val password: String = "")

/** Only public account information is returned to the client. */
data class LoginUser(val id: Long, val username: String, val admin: Boolean)

/** Checks database accounts using BCrypt instead of hardcoded Android credentials. */
@Service
class AuthService(private val users: UserRepository) {
    private val encoder = BCryptPasswordEncoder(12)
    private val dummyHash = encoder.encode("unused-timing-placeholder")

    /** Returns the verified user, or the same error for an unknown user and a wrong password. */
    fun login(credentials: Credentials): LoginUser {
        validate(credentials, registering = false)
        val user = users.findByUsername(credentials.username.trim())
        val hash = user?.passwordHash
        val matches = encoder.matches(credentials.password, hash ?: dummyHash)
        if (user == null || hash == null || !matches) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong username or password")
        }
        return publicUser(user)
    }

    /** Creates a regular account; existing accounts can never be overwritten here. */
    fun register(credentials: Credentials): LoginUser {
        validate(credentials, registering = true)
        val username = credentials.username.trim()
        if (users.findByUsername(username) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Username already exists")
        }
        return try {
            publicUser(users.saveAndFlush(User(username = username,
                passwordHash = encoder.encode(credentials.password))))
        } catch (_: DataIntegrityViolationException) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Account could not be created")
        }
    }

    private fun validate(credentials: Credentials, registering: Boolean) {
        if (credentials.username.isBlank() || credentials.username.trim().length > 100 ||
            credentials.password.isBlank() || credentials.password.toByteArray(Charsets.UTF_8).size > 72 ||
            (registering && credentials.password.length < 6)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a username and a password (6–72 bytes for registration)")
        }
    }

    private fun publicUser(user: User) = LoginUser(requireNotNull(user.id), user.username, user.isAdmin)
}

/** Username/password endpoints; OAuth2 and protected API sessions are separate work. */
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val auth: AuthService) {
    /** Checks a stored password and returns the corresponding account. */
    @PostMapping("/login")
    fun login(@RequestBody credentials: Credentials) = auth.login(credentials)

    /** Allows the existing sign-up page to provision accounts for database login. */
    @PostMapping("/register")
    fun register(@RequestBody credentials: Credentials): ResponseEntity<LoginUser> =
        ResponseEntity.status(HttpStatus.CREATED).body(auth.register(credentials))

    /** Returns a consistent error body without exposing credentials or database details. */
    @ExceptionHandler(ResponseStatusException::class)
    fun authenticationError(exception: ResponseStatusException): ResponseEntity<ProblemDetail> =
        ResponseEntity.status(exception.statusCode).body(
            ProblemDetail.forStatusAndDetail(exception.statusCode, exception.reason ?: "Request failed"))
}
