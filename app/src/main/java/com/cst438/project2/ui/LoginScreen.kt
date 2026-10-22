package com.cst438.project2.ui

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.cst438.project2.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import androidx.credentials.exceptions.NoCredentialException
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding


private const val SERVER_CLIENT_ID =
    "878293662-pg8la953ki9obc1880uervpgact6mvji.apps.googleusercontent.com"
suspend fun signInWithGoogle(context: Context): String {
    val credentialManager = CredentialManager.create(context)

    val googleOption = GetGoogleIdOption.Builder()
        .setServerClientId(SERVER_CLIENT_ID)
        .setFilterByAuthorizedAccounts(false)
        .setAutoSelectEnabled(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleOption)
        .build()

    val result = credentialManager.getCredential(
        context,
        request
    )

    val credential = result.credential

    if (
        credential is CustomCredential &&
        credential.type ==
        GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        val googleCredential =
            GoogleIdTokenCredential.createFrom(credential.data)

        return googleCredential.idToken
    }

    throw IllegalStateException("Unexpected credential type")
}

suspend fun authenticateWithBackend(idToken: String): Boolean =
    withContext(Dispatchers.IO) {
        val baseUrl = com.cst438.project2.BuildConfig.API_BASE_URL.trimEnd('/')
        val url = URL("$baseUrl/api/v1/users/me")
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.setRequestProperty(
                "Authorization",
                "Bearer $idToken"
            )
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000

            connection.responseCode in 200..299
        } finally {
            connection.disconnect()
        }
    }

/** Login form backed by the remote user database. */
@Composable
fun LoginScreen(
    onSignUpClick: () -> Unit = {},
    AdminLogin: () -> Unit = {},
    EventPage: (String) -> Unit = {},
    authenticate: suspend (String, String) -> com.cst438.project2.auth.AuthUser = { username, password ->
        com.cst438.project2.auth.AuthApi().login(username, password)
    }
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painter = painterResource(R.drawable.login_logo), contentDescription = "Login logo",
            modifier = Modifier.fillMaxWidth().height(180.dp), contentScale = ContentScale.Fit)
        Text("Login to EventHub")
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = username, onValueChange = { username = it },
            label = { Text("Username") }, singleLine = true, enabled = !loading,
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = password, onValueChange = { password = it },
            label = { Text("Password") }, singleLine = true, enabled = !loading,
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))
        Button(enabled = !loading, modifier = Modifier.fillMaxWidth(), onClick = {
            if (username.isBlank() || password.isBlank()) {
                message = "Username and password are required"
            } else {
                loading = true
                message = "Signing in…"
                scope.launch {
                    try {
                        val user = authenticate(username.trim(), password)

                        if (user.isAdmin) {
                            password = ""
                            message = "Admin login successful"
                            AdminLogin()
                        } else {
                            val token = user.accessToken

                            if (token.isNullOrBlank()) {
                                message = "Login succeeded, but no access token was returned"
                            } else {
                                password = ""
                                message = "Login successful"
                                EventPage(token)
                            }

                        }
                    } catch (cancelled: kotlinx.coroutines.CancellationException) {
                        throw cancelled
                    } catch (error: java.io.IOException) {
                        message = error.message?.takeIf { it.isNotBlank() } ?: "Cannot connect to the server"
                    } catch (error: Exception) {
                        message = error.message?.takeIf { it.isNotBlank() }
                            ?: "Could not sign in. Please try again"
                    } finally {
                        loading = false
                    }
                }
            }
        }) { Text(if (loading) "Signing in…" else "Login") }
        Text("or", modifier = Modifier.padding(top = 8.dp))
        Button(enabled = !loading, modifier = Modifier.fillMaxWidth(), onClick = {
            scope.launch {
                try {
                    val idToken = signInWithGoogle(context)
                    if (authenticateWithBackend(idToken)) {
                        message = "Logged in with Google"
                        EventPage(idToken)
                    } else {
                        message = "Backend rejected Google token"
                    }
                } catch (cancelled: kotlinx.coroutines.CancellationException) {
                    throw cancelled
                } catch (e: NoCredentialException) {
                    message = "No Google account is available on this device"
                } catch (e: Exception) {
                    message = "Google sign-in failed: ${e.message}"
                }
            }
        }) { Text("Sign in with Google") }
        if(message.isNotBlank()) {
            Text (text = message, modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
        }
        TextButton(onClick = onSignUpClick, enabled = !loading) { Text("Create an account") }
    }
}