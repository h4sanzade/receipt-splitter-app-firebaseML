package com.hasanzade.germanstyle.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
        // Header
        Text(
            text = "AI Receipt Splitter",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Add friends to split the bill with AI-powered receipt scanning",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Add Friend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = newFriendName,
                    onValueChange = {
                        newFriendName = it
                        nameError = ValidationUtils.getFriendNameError(it)
                    },
                    label = { Text("Friend's name") },
                    placeholder = { Text("Enter friend's name") },
                    isError = nameError != null,
                    supportingText = {
                        nameError?.let { error ->
                            Text(text = error, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Add, contentDescription = null)
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
                        .padding(top = 12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Friend")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (friends.isNotEmpty()) {
            Text(
                text = "Friends Added (${friends.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No friends added yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Add at least one friend to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column {
            if (friends.isEmpty()) {
                OutlinedButton(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Skip and Go to AI Scanner")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "You can add friends later after scanning the receipt with Gemini AI",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue to AI Scanner")
                }
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
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = friendName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Ready to split bills with AI",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onRemove,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove $friendName"
                )
            }
        }
    }
}
