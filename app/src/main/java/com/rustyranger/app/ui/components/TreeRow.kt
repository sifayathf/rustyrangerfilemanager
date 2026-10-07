package com.rustyranger.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustyranger.app.ui.model.TreeNode
import com.rustyranger.app.ui.theme.GoldFolder
import com.rustyranger.app.ui.theme.SoftBlue
import com.rustyranger.app.ui.theme.TextMuted
import com.rustyranger.app.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TreeRow(
    treeNode: TreeNode,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    showFileMetadata: Boolean,
    padMult: Float,
    fontPadMult: Float,
    onExpandClick: () -> Unit,
    onNodeTap: () -> Unit,
    onRowLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val node = treeNode.node
    val bg = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
             else Color.Transparent

    val formattedDate = remember(node.modifiedTimestamp) {
        if (node.modifiedTimestamp <= 0) ""
        else {
            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            sdf.format(Date(node.modifiedTimestamp * 1000))
        }
    }

    val extension = remember(node.name, node.isDirectory) {
        if (node.isDirectory) "" 
        else {
            val idx = node.name.lastIndexOf('.')
            if (idx > 0 && idx < node.name.length - 1) node.name.substring(idx + 1).lowercase()
            else "file"
        }
    }

    // Dynamic Density Scaling (Proportional to padMult)
    val rowHeight = (48 * padMult).dp
    val fontSizeMain = (15 * fontPadMult).sp
    val fontSizeSub = (12 * fontPadMult).sp
    val iconSize = (22 * padMult).coerceAtLeast(12f).dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(rowHeight)
            .background(bg)
            .combinedClickable(
                onClick = onNodeTap,
                onLongClick = onRowLongClick
            )
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tree Indentation & Connector Lines
        if (treeNode.depth > 0) {
            TreeConnectorLines(
                depth = treeNode.depth,
                isLastChild = treeNode.isLastChild,
                ancestorIsLast = treeNode.ancestorIsLast,
                modifier = Modifier.fillMaxHeight()
            )
        } else {
            Spacer(modifier = Modifier.width(4.dp))
        }

        // Expand / Collapse Button (Folders only)
        if (node.isDirectory) {
            ExpandButton(
                isExpanded = treeNode.isExpanded,
                onClick = onExpandClick
            )
        } else {
            Spacer(modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(4.dp))

        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onNodeTap() },
                colors = CheckboxDefaults.colors(checkedColor = SoftBlue),
                modifier = Modifier.size(20.dp).padding(end = 2.dp)
            )
        }

        // Folder / File Icon & Name Row (Navigates into folder or opens file)
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClick = onNodeTap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (node.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                contentDescription = null,
                tint = if (node.isDirectory) GoldFolder else SoftBlue,
                modifier = Modifier.size(iconSize)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = node.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSizeMain),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (showFileMetadata) {
            // Size Column
            Text(
                text = if (node.isDirectory) "" else formatSize(node.sizeBytes),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSizeSub),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.width(64.dp)
            )

            // Type Column (Blank for folders, extension for files)
            Text(
                text = extension,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = fontSizeSub),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(40.dp)
            )

            // Date Modified Column
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = (fontSizeSub.value - 1).sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.width(105.dp)
            )
        }
    }
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return ""
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format("%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format("%.1f MB", mb)
    val gb = mb / 1024.0
    return String.format("%.1f GB", gb)
}
