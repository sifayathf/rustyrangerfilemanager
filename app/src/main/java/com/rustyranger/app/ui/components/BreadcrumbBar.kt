package com.rustyranger.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustyranger.app.ui.theme.SoftBlue
import com.rustyranger.app.ui.theme.TextMuted
import com.rustyranger.app.ui.theme.TextPrimary
import com.rustyranger.app.util.FileOpener
import java.io.File

@Composable
fun BreadcrumbBar(
    currentUri: String,
    onSegmentClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cleanPath = remember(currentUri) { FileOpener.cleanPath(currentUri) }
    val segments = remember(cleanPath) {
        val pathFile = File(cleanPath)
        val list = mutableListOf<Pair<String, String>>() // Name to Full Uri
        var current: File? = pathFile
        while (current != null && current.name.isNotEmpty()) {
            list.add(0, Pair(current.name, "file://${current.absolutePath}"))
            current = current.parentFile
        }
        if (list.isEmpty()) {
            list.add(Pair("storage", "file:///$cleanPath"))
        }
        list
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = "Home Root",
            tint = SoftBlue,
            modifier = Modifier
                .size(18.dp)
                .clickable { onSegmentClick("file:///storage/emulated/0") }
        )

        segments.forEachIndexed { index, (segmentName, targetUri) ->
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )

            val isLast = index == segments.lastIndex
            Text(
                text = segmentName,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = if (isLast) TextPrimary else SoftBlue,
                modifier = Modifier
                    .clickable { onSegmentClick(targetUri) }
                    .padding(horizontal = 4.dp)
            )
        }
    }
}
