package com.cst438.project2.auth

import com.cst438.project2.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Public user data returned only after the backend accepts the credentials. */
data class AuthUser(val id: Long, val username: String, val isAdmin: Boolean, val accessToken: String? = null)

/** Calls the backend on an IO dispatcher; the Android app never connects directly to Neon. */
class AuthApi(private val baseUrl: String = BuildConfig.API_BASE_URL) {
    /** Verifies credentials against the backend database. */
    suspend fun login(username: String, password: String): AuthUser = request("login", username, password)

    /** Creates an account with a server-side password hash. */
    suspend fun register(username: String, password: String): AuthUser = request("register", username, password)

    private suspend fun request(action: String, username: String, password: String): AuthUser =
        withContext(Dispatchers.IO) {
            val connection = URL("${baseUrl.trimEnd('/')}/api/v1/auth/$action").openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 10_000
                connection.readTimeout = 15_000
                connection.instanceFollowRedirects = false
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.setRequestProperty("Accept", "application/json, application/problem+json")
                val payload = JSONObject().put("username", username.trim()).put("password", password)
                val bytes = payload.toString().toByteArray(Charsets.UTF_8)
                connection.setFixedLengthStreamingMode(bytes.size)
                connection.outputStream.use { it.write(bytes) }

                val status = connection.responseCode
                if (status !in 200..299) {
                    val serverDetail = try {
                        val errText = connection.errorStream?.bufferedReader()?.use { it.readText() }
                        if (!errText.isNullOrEmpty()) JSONObject(errText).optString("detail").ifBlank { null } else null
                    } catch (_: Exception) { null }

                    throw IOException(serverDetail ?: when (status) {
                        400 -> "Check your username and password requirements"
                        401 -> "Wrong username or password"
                        409 -> "Username already exists"
                        else -> "Server error ($status). Please try again later"
                    })
                }
                val json = connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
                AuthUser(
                    id = json.getLong("id"),
                    username = json.getString("username"),
                    isAdmin = json.getBoolean("admin"),
                    accessToken = json.optString("accessToken").takeIf { it.isNotBlank() }
                )
            } finally {
                connection.disconnect()
            }
        }
}
