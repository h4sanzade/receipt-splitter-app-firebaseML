package com.hasanzade.germanstyle.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hasanzade.germanstyle.data.ReceiptItem
import com.hasanzade.germanstyle.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentScreen(
    items: List<ReceiptItem>,
    friends: List<String>,
    onToggleAssignment: (String, String) -> Unit,
    onCalculate: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "AI-Extracted Items",
                style = MaterialTheme.typography.headlineSmall
            )

            IconButton(onClick = onCalculate) {
                Icon(Icons.Default.Calculate, contentDescription = "Calculate")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Gemini AI Successfully Extracted ${items.size} Items",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Tap on friend names to assign items. Items can be shared among multiple friends.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                ItemAssignmentCard(
                    item = item,
                    friends = friends,
                    onToggleAssignment = onToggleAssignment
                )
            }
        }

        AssignmentSummaryCard(
            items = items,
            modifier = Modifier.padding(top = 16.dp)
        )

        Button(
            onClick = onCalculate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = items.any { it.assignedFriends.isNotEmpty() }
        ) {
            Icon(Icons.Default.Calculate, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Calculate Split")
        }
    }
}

@Composable
fun ItemAssignmentCard(
    item: ReceiptItem,
    friends: List<String>,
    onToggleAssignment: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "AI Detected",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = FormatUtils.formatItemDetails(item.quantity, item.unit_price, item.total_price),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Assign to:",
                style = MaterialTheme.typography.labelMedium
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(friends) { friend ->
                    val isAssigned = item.isAssignedTo(friend)
                    FilterChip(
                        onClick = { onToggleAssignment(item.id, friend) },
                        label = { Text(friend) },
                        selected = isAssigned
                    )
                }
            }

            if (item.assignedFriends.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = FormatUtils.formatSplitInfo(item.assignedFriends.size, item.total_price),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AssignmentSummaryCard(
    items: List<ReceiptItem>,
    modifier: Modifier = Modifier
) {
    val totalAmount = items.sumOf { it.total_price }
    val assignedAmount = items.filter { it.assignedFriends.isNotEmpty() }.sumOf { it.total_price }
    val unassignedAmount = totalAmount - assignedAmount

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (unassignedAmount > 0)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "AI-Extracted Receipt Summary",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Amount:")
                Text(FormatUtils.formatCurrency(totalAmount))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Assigned:")
                Text(FormatUtils.formatCurrency(assignedAmount))
            }
            if (unassignedAmount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Unassigned:",
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        FormatUtils.formatCurrency(unassignedAmount),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
