package com.cst438.project2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cst438.project2.auth.AdminAccount
import com.cst438.project2.auth.AdminApi
import com.cst438.project2.auth.AuthSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.IOException

@Composable
fun AdminScreen(logout: () -> Unit) {
    val token = AuthSession.googleToken
    val api = remember { AdminApi() }
    val scope = rememberCoroutineScope()

    var users by remember(token) {
        mutableStateOf<List<AdminAccount>>(emptyList())
    }
    var loading by remember(token) { mutableStateOf(false) }
    var error by remember(token) { mutableStateOf<String?>(null) }
    var refresh by remember { mutableStateOf(0) }
    var selectedUser by remember(token) {
        mutableStateOf<AdminAccount?>(null)
    }
    var deleting by remember(token) { mutableStateOf(false) }

    LaunchedEffect(token, refresh) {
        if (token.isNullOrBlank()) {
            error = "Please sign in with Google to manage users"
            return@LaunchedEffect
        }

        loading = true
        error = null

        try {
            users = api.getUsers(token)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (exception: IOException) {
            error = exception.message ?: "Cannot connect to the server"
        } catch (_: Exception) {
            error = "Could not load users. Please try again"
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "Admin Page",
            style = MaterialTheme.typography.headlineMedium
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                enabled = !loading && !deleting &&
                        !token.isNullOrBlank(),
                onClick = { refresh++ }
            ) {
                Text("Refresh")
            }

            TextButton(
                onClick = {
                    AuthSession.clear()
                    logout()
                }
            ) {
                Text("Logout")
            }
        }

        Spacer(Modifier.height(16.dp))

        if (loading || deleting) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
        }

        error?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )

            if (!token.isNullOrBlank()) {
                TextButton(
                    enabled = !loading && !deleting,
                    onClick = { refresh++ }
                ) {
                    Text("Retry")
                }
            }

            Spacer(Modifier.height(12.dp))
        }

        if (!loading && error == null && users.isEmpty()) {
            Text("No users found")
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(users, key = { it.id }) { user ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = user.username ?: "Google account",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text("ID: ${user.id}")
                        Text("Role: ${user.role}")

                        Spacer(Modifier.height(8.dp))

                        Button(
                            enabled = !loading && !deleting &&
                                    !token.isNullOrBlank(),
                            onClick = { selectedUser = user }
                        ) {
                            Text("Remove")
                        }
                    }
                }
            }
        }
    }

    selectedUser?.let { user ->
        AlertDialog(
            onDismissRequest = { selectedUser = null },
            title = { Text("Delete user?") },
            text = {
                Text(
                    "Delete ${user.username ?: "Google account"} " +
                            "(ID: ${user.id})? Their organized events " +
                            "and related RSVPs will also be removed."
                )
            },
            dismissButton = {
                TextButton(onClick = { selectedUser = null }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedUser = null

                        if (!token.isNullOrBlank()) {
                            deleting = true
                            error = null

                            scope.launch {
                                try {
                                    api.deleteUser(token, user.id)
                                    users = users.filterNot {
                                        it.id == user.id
                                    }
                                } catch (cancelled: CancellationException) {
                                    throw cancelled
                                } catch (exception: IOException) {
                                    error = exception.message
                                        ?: "Could not delete user"
                                } catch (_: Exception) {
                                    error = "Could not delete user"
                                } finally {
                                    deleting = false
                                }
                            }
                        }
                    }
                ) {
                    Text("Delete")
                }
            }
        )
    }
}