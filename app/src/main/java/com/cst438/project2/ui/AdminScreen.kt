package com.cst438.project2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.safeDrawingPadding


data class AdminUser (
    val id: Long,
    val email: String,
    val displayName: String,
    val role: String
)

@Composable
fun AdminScreen(logout: () -> Unit) {
    //temp sign in  credentials
    val users = listOf (
        AdminUser(1, "linus@email.com", "Linus Schaub", "USER"),
        AdminUser(2, "hujo@email.com", "Hugo Ruiz-Mireles", "USER"),
        AdminUser(3, "victor@email.com", "Victor Borba", "ADMIN")


    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)

    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Admin Page",
                style = MaterialTheme.typography.headlineMedium
            )

            Button(
                onClick = logout
            ) {
                Text("Logout")


            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text (
            text = "Manage users",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(users) {
                user -> UserCard(user)
            }

        }


    }

}

@Composable
fun UserCard(user: AdminUser) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = user.displayName,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Role: ${user.role}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        // Nothing for now
                    }
                ) {
                    Text("Remove")
                }


            }
        }
    }
}