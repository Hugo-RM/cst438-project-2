package com.example.cst438project2.backend.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import com.example.cst438project2.backend.entity.User
import com.example.cst438project2.backend.repository.UserRepository
import com.example.cst438project2.backend.service.AppTokenService
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
data class LoginUser(
    val id: Long,
    val username: String,
    val admin: Boolean,
    val accessToken: String? = null
)

/** Checks database accounts using BCrypt instead of hardcoded Android credentials. */
@Service
class AuthService(private val users: UserRepository, private val appTokenService: AppTokenService) {
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
        return publicUser(user).copy(accessToken = appTokenService.createToken(user))
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
        } catch (e: DataIntegrityViolationException) {
            e.printStackTrace()
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Account could not be created"
            )

        }
    }

    private fun validate(credentials: Credentials, registering: Boolean) {
        if (credentials.username.isBlank() || credentials.username.trim().length > 100 ||
            credentials.password.isBlank() || credentials.password.toByteArray(Charsets.UTF_8).size > 72 ||
            (registering && credentials.password.length < 6)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a username and a password (6–72 bytes for registration)")
        }
    }

    private fun publicUser(user: User): LoginUser {
        return LoginUser(
            id = requireNotNull(user.id),
            username = requireNotNull(user.username) {
                "Username is required for password-based accounts"
            },

            admin = user.isAdmin
        )
    }
}

/** Username/password endpoints; OAuth2 and protected API sessions are separate work. */
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val auth: AuthService) {
    /** Checks a stored password and returns the corresponding account. */
    @Operation(summary = "Log in with a username and password")
    @ApiResponse(responseCode = "200", description = "Credentials are valid; returns an access token")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Wrong username or password")
    @PostMapping("/login")
    fun login(@RequestBody credentials: Credentials) = auth.login(credentials)

    /** Allows the existing sign-up page to provision accounts for database login. */
    @Operation(summary = "Create a username/password account")
    @ApiResponse(responseCode = "201", description = "Account created")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "409", description = "Username already exists")
    @PostMapping("/register")
    fun register(@RequestBody credentials: Credentials): ResponseEntity<LoginUser> =
        ResponseEntity.status(HttpStatus.CREATED).body(auth.register(credentials))

    /** Returns a consistent error body without exposing credentials or database details. */
    @ExceptionHandler(ResponseStatusException::class)
    fun authenticationError(exception: ResponseStatusException): ResponseEntity<ProblemDetail> =
        ResponseEntity.status(exception.statusCode).body(
            ProblemDetail.forStatusAndDetail(exception.statusCode, exception.reason ?: "Request failed"))
}
