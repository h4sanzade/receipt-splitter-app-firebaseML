package com.hasanzade.germanstyle.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hasanzade.germanstyle.utils.ValidationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendManagementScreen(
    friends: List<String>,
    onAddFriend: (String) -> Unit,
    onRemoveFriend: (String) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newFriendName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Add Friends to Split With",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Add Friend Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                OutlinedTextField(
                    value = newFriendName,
                    onValueChange = {
                        newFriendName = it
                        nameError = ValidationUtils.getFriendNameError(it)
                    },
                    label = { Text("Friend's name") },
                    isError = nameError != null,
                    supportingText = {
                        nameError?.let { error ->
                            Text(text = error, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (ValidationUtils.isValidFriendName(newFriendName)) {
                            onAddFriend(newFriendName)
                            newFriendName = ""
                            nameError = null
                        }
                    },
                    enabled = ValidationUtils.isValidFriendName(newFriendName),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Add Friend")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Friends List
        if (friends.isNotEmpty()) {
            Text(
                text = "Friends (${friends.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(friends) { friend ->
                    FriendListItem(
                        friendName = friend,
                        onRemove = { onRemoveFriend(friend) }
                    )
                }
            }
        } else {
            Text(
                text = "No friends added yet. Add at least one friend to continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 32.dp)
            )
        }

        // Next Button
        if (friends.isNotEmpty()) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Continue to Camera")
            }
        }
    }
}

@Composable
fun FriendListItem(
    friendName: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = friendName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove $friendName",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}