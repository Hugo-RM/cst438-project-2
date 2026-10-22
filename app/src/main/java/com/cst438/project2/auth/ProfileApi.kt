package com.cst438.project2.auth

import com.cst438.project2.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class UserProfile(
    val id: Long,
    val username: String?,
    val role: String
)

class ProfileApi(
    private val baseUrl: String = BuildConfig.API_BASE_URL
) {
    suspend fun getProfile(): UserProfile {
        val authorization = AuthSession.authorizationHeader
            ?: throw IOException("Please sign in again")

        return withContext(Dispatchers.IO) {
            val connection = URL(
                "${baseUrl.trimEnd('/')}/api/v1/users/me"
            ).openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 10_000
                connection.readTimeout = 15_000
                connection.instanceFollowRedirects = false
                connection.setRequestProperty(
                    "Authorization",
                    authorization
                )
                connection.setRequestProperty(
                    "Accept",
                    "application/json"
                )

                val status = connection.responseCode

                if (status !in 200..299) {
                    throw IOException(
                        when (status) {
                            401 -> "Please sign in again"
                            403 -> "Access to your profile was denied"
                            else -> "Server error ($status). Please try again"
                        }
                    )
                }

                val json = connection.inputStream
                    .bufferedReader()
                    .use { JSONObject(it.readText()) }

                UserProfile(
                    id = json.getLong("id"),
                    username = if (json.isNull("username")) {
                        null
                    } else {
                        json.getString("username")
                    },
                    role = if (json.getBoolean("admin")) {
                        "ADMIN"
                    } else {
                        "USER"
                    }
                )
            } finally {
                connection.disconnect()
            }
        }
    }
}