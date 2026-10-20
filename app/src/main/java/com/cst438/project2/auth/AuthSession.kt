package com.cst438.project2.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AuthSession {
    var googleToken: String? by mutableStateOf(null)
        private set

    fun updateGoogleToken(token: String) {
        require(token.isNotBlank()) {
            "Google token must not be blank"
        }

        googleToken = token
    }

    fun clear() {
        googleToken = null
    }
}