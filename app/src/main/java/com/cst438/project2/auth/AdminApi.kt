package com.cst438.project2.auth

import com.cst438.project2.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class AdminAccount(
    val id: Long,
    val username: String?,
    val role: String
)

class AdminApi(
    private val baseUrl: String = BuildConfig.API_BASE_URL
) {
    suspend fun getUsers(token: String): List<AdminAccount> {
        val response = request(
            method = "GET",
            path = "/api/v1/users",
            token = token
        )

        val users = JSONArray(response)

        return List(users.length()) { index ->
            val user = users.getJSONObject(index)

            AdminAccount(
                id = user.getLong("id"),
                username = if (user.isNull("username")) {
                    null
                } else {
                    user.getString("username")
                },
                role = user.getString("role")
            )
        }
    }
    suspend fun getCurrentUser(token: String): AdminAccount {
        val response = request(
            method = "GET",
            path = "/api/v1/users/me",
            token = token
        )

        val user = org.json.JSONObject(response)

        return AdminAccount(
            id = user.getLong("id"),
            username = if (user.isNull("username")) {
                null
            } else {
                user.getString("username")
            },
            role = if (user.getBoolean("admin")) {
                "ADMIN"
            } else {
                "USER"
            }
        )
    }
    suspend fun deleteUser(token: String, userId: Long) {
        request(
            method = "DELETE",
            path = "/api/v1/users/$userId",
            token = token
        )
    }

    private suspend fun request(
        method: String,
        path: String,
        token: String
    ): String = withContext(Dispatchers.IO) {
        if (token.isBlank()) {
            throw IOException("Please sign in with Google first")
        }

        val connection = URL(
            "${baseUrl.trimEnd('/')}$path"
        ).openConnection() as HttpURLConnection

        try {
            connection.requestMethod = method
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty(
                "Authorization",
                "Bearer $token"
            )
            connection.setRequestProperty(
                "Accept",
                "application/json"
            )

            val status = connection.responseCode

            if (status !in 200..299) {
                val message = when (status) {
                    401 -> "Your session has expired. Please sign in again"
                    403 -> "Administrator permission required"
                    404 -> "User no longer exists"
                    409 -> "This account cannot be deleted"
                    else -> "Server error ($status). Please try again"
                }

                throw IOException(message)
            }

            if (status == HttpURLConnection.HTTP_NO_CONTENT) {
                ""
            } else {
                connection.inputStream.bufferedReader().use {
                    it.readText()
                }
            }
        } finally {
            connection.disconnect()
        }
    }
}