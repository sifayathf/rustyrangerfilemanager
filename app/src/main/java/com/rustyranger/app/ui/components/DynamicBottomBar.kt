package com.rustyranger.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustyranger.app.ui.theme.DarkSurface
import com.rustyranger.app.ui.theme.DarkSurfaceVariant
import com.rustyranger.app.ui.theme.TextMuted
import com.rustyranger.app.ui.theme.TextPrimary

@Composable
fun DynamicBottomBar(
    selectedCount: Int,
    totalCount: Int,
    selectedSize: Long,
    totalSize: Long,
    isSelectionMode: Boolean,
    onHome: () -> Unit,
    onUp: () -> Unit,
    onToggleMultiSelect: () -> Unit,
    onSelectAll: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onPaste: () -> Unit,
    hasClipboard: Boolean,
    onMove: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onMore: () -> Unit,
    onCancelSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .navigationBarsPadding() // FIX: Prevents occlusion by Android system gesture bar
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                .padding(horizontal = 4.dp), // Tighter edge padding
            horizontalArrangement = Arrangement.SpaceBetween, // More even spacing across the entire width
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isSelectionMode && selectedCount == 0) {
                // Normal Toolbar Mode
                BottomNavItem(icon = Icons.Default.Home, label = "Home", onClick = onHome)
                BottomNavItem(icon = Icons.Default.ArrowUpward, label = "Up", onClick = onUp)
                BottomNavItem(icon = Icons.Default.CheckBox, label = "Multi Select", onClick = onToggleMultiSelect)
                if (hasClipboard) {
                    BottomNavItem(icon = Icons.Default.ContentPaste, label = "Paste", onClick = onPaste)
                }
                BottomNavItem(icon = Icons.Default.MoreHoriz, label = "More", onClick = onMore)
            } else {
                // Selection Mode
                BottomNavItem(icon = Icons.Default.SelectAll, label = "Select All", onClick = onSelectAll)
                BottomNavItem(icon = Icons.Default.ContentCopy, label = "Copy", onClick = onCopy)
                BottomNavItem(icon = Icons.Default.ContentCut, label = "Cut", onClick = onCut)
                BottomNavItem(icon = Icons.Default.Delete, label = "Delete", onClick = onDelete)
                BottomNavItem(icon = Icons.Default.Edit, label = "Rename", onClick = onRename)
                BottomNavItem(icon = Icons.Default.Close, label = "Cancel", onClick = onCancelSelection)
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 1.dp)

        // Bottom Status Line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$selectedCount / $totalCount",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "${formatBytes(selectedSize)} / ${formatBytes(totalSize)}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "RW",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(16.dp) // Slightly smaller icon
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp), // Dense small text
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format("%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format("%.1f MB", mb)
    val gb = mb / 1024.0
    return String.format("%.1f GB", gb)
}
