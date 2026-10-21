package com.cst438.project2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cst438.project2.auth.ProfileApi
import com.cst438.project2.auth.UserProfile
import kotlinx.coroutines.CancellationException
import java.io.IOException

@Composable
fun ProfileScreen(
    loadProfile: suspend () -> UserProfile = {
        ProfileApi().getProfile()
    }
) {
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableStateOf(0) }

    LaunchedEffect(refresh) {
        loading = true
        error = null
        profile = null

        try {
            profile = loadProfile()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (exception: IOException) {
            error = exception.message ?: "Cannot connect to the server"
        } catch (_: Exception) {
            error = "Could not load your profile"
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "My Profile",
            style = MaterialTheme.typography.headlineMedium
        )

        if (loading) {
            CircularProgressIndicator()
            Text("Loading profile…")
        }

        error?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )

            Button(onClick = { refresh++ }) {
                Text("Retry")
            }
        }

        profile?.let { user ->
            Text("Username: ${user.username ?: "Not set"}")
            Text("Role: ${user.role}")
            Text("User ID: ${user.id}")
        }


    }
}