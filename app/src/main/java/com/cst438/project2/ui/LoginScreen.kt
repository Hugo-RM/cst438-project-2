package com.cst438.project2.ui

import android.content.Context
import androidx.compose.foundation.Image
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
        val url = URL("http://10.0.2.2:8080/api/v1/users/me")
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
@Composable
fun LoginScreen(
    AdminLogin: () -> Unit,
    EventPage: () -> Unit,
    onSignUpClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var username by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.login_logo),
            contentDescription = "Login logo",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentScale = ContentScale.Fit
        )

        Text(text = "Login to EventHub")

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (username == "Justin" && password == "hello") {
                    EventPage()
                } else if (username == "Victor" && password == "admin123") {
                    AdminLogin()
                } else {
                    message = "Wrong username or password"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "or")

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        val idToken = signInWithGoogle(context)
                        val loggedIn = authenticateWithBackend(idToken)

                        if (loggedIn) {
                            message = "Logged in with Google"
                            EventPage()
                        } else {
                            message = "Backend rejected Google token"
                        }
                    } catch (e: NoCredentialException) {
                        message = "No Google account is available on this device"
                    } catch (e: Exception) {
                        message = "Google sign-in failed: ${e.message}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign in with Google")
        }

        Spacer(modifier = Modifier.height(15.dp))

        Text(text = message)

        TextButton(onClick = onSignUpClick) {
            Text("Create an account")
        }
    }
}