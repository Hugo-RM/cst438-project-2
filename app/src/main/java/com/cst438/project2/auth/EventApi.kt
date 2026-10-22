package com.cst438.project2.auth


import com.cst438.project2.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
data class EventCategory(
    val id: Long,
    val name: String
)

data class EventItem(
    val id: Long,
    val title: String,
    val description: String,
    val location: String,
    val startsAt: String,
    val endsAt: String,
    val categoryName: String
)

class EventApi(
    private val baseUrl: String = BuildConfig.API_BASE_URL
) {
suspend fun getCategories(): List<EventCategory> = withContext(Dispatchers.IO) {
        val json = request("GET", "/api/v1/categories")

        val array = when (json) {
            is JSONArray -> json
            is JSONObject -> json.optJSONArray("content") ?: JSONArray()
            else -> JSONArray()
        }

        val categories = mutableListOf<EventCategory>()

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)

            categories.add(
                EventCategory(
                    id = item.getLong("id"),
                    name = item.getString("name")
                )
            )
        }

        categories
    }

    suspend fun getEvents(): List<EventItem> =
        withContext(Dispatchers.IO) {
            val json = request("GET", "/api/v1/events")
            val array = (json as JSONObject).optJSONArray("content")
                ?: JSONArray()

            val events = mutableListOf<EventItem>()

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                events.add(
                    EventItem(
                        id = item.getLong("id"),
                        title = item.getString("title"),
                        description = item.optString("description", ""),
                        location = item.getString("location"),
                        startsAt = item.getString("startsAt"),
                        endsAt = item.getString("endsAt"),
                        categoryName = item.optString("categoryName", "")
                    )
                )
            }

            events
        }

    suspend fun createEvent(
        title: String,
        description: String,
        categoryId: Long,
        location: String,
        eventDate: String,
        startTime: String,
        endTime: String,
        accessToken: String
    ): JSONObject = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("title", title)
            .put("description", description)
            .put("categoryId", categoryId)
            .put("location", location)
            .put("startsAt", "${eventDate}T${startTime}:00")
            .put("endsAt", "${eventDate}T${endTime}:00")
            .put("status", "PUBLISHED")

        request(
            method = "POST",
            path = "/api/v1/events",
            payload = payload,
            accessToken = accessToken
        ) as JSONObject
    }

    private fun request(
        method: String,
        path: String,
        payload: JSONObject? = null,
        accessToken: String? = null
    ): Any {
        val connection= URL("${baseUrl.trimEnd('/')}$path").openConnection() as HttpURLConnection

        try {
            connection.requestMethod = method
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("Accept", "application/json")

            if (payload != null) {
                connection.doOutput = true
                connection.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=utf-8"
                )
            }

            if (!accessToken.isNullOrBlank()) {
                connection.setRequestProperty(
                    "Authorization",
                    "Bearer $accessToken"
                )
            }

            if (payload != null) {
                val bytes = payload.toString().toByteArray(Charsets.UTF_8)
                connection.outputStream.use {
                    it.write(bytes)
                }
            }

            val status = connection.responseCode
            val stream = if (status in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val responseText = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (status !in 200..299) {
                throw IOException("Event request failed ($status): $responseText")

            }

            return if (responseText.trimStart().startsWith("[")) {
                JSONArray(responseText)
            } else {
                JSONObject(responseText)
            }
        } finally {
            connection.disconnect()
        }
    }
}
