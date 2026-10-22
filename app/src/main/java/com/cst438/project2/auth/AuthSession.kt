package com.cst438.project2.auth

import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AuthSession {
    var googleToken: String? by mutableStateOf(null)
        private set

    private var passwordAuthorization: String? by mutableStateOf(null)

    val authorizationHeader: String?
        get() = googleToken?.let { "Bearer $it" }
            ?: passwordAuthorization

    fun updateGoogleToken(token: String) {
        require(token.isNotBlank()) {
            "Google token must not be blank"
        }

        passwordAuthorization = null
        googleToken = token
    }

    fun updatePasswordCredentials(
        username: String,
        password: String
    ) {
        require(username.isNotBlank() && password.isNotBlank())
        require(':' !in username) {
            "Username must not contain a colon"
        }

        val credentials = "$username:$password"
        val encoded = Base64.encodeToString(
            credentials.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP
        )

        googleToken = null
        passwordAuthorization = "Basic $encoded"
    }

    fun clear() {
        googleToken = null
        passwordAuthorization = null
    }
}