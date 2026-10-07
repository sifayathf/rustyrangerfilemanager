package com.rustyranger.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.rustyranger.app.ui.theme.DarkSurfaceVariant
import com.rustyranger.app.ui.theme.TextPrimary

@Composable
fun BottomActionBar(
    selectedCount: Int,
    onNewFolder: () -> Unit,
    onSearch: () -> Unit,
    onSort: () -> Unit,
    onSelectAll: () -> Unit,
    onPaste: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(16.dp)),
        color = DarkSurfaceVariant.copy(alpha = 0.95f),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNewFolder) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder", tint = TextPrimary)
            }
            IconButton(onClick = onSearch) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
            }
            IconButton(onClick = onSort) {
                Icon(Icons.Default.Sort, contentDescription = "Sort", tint = TextPrimary)
            }
            IconButton(onClick = onSelectAll) {
                Icon(Icons.Default.SelectAll, contentDescription = "Select All", tint = TextPrimary)
            }
            IconButton(onClick = onPaste) {
                Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = TextPrimary)
            }
        }
    }
}
