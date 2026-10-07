package com.rustyranger.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rustyranger.app.data.ffi.FfiVfsNode
import com.rustyranger.app.util.SettingsStore

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridItem(
    node: FfiVfsNode,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    showImageThumbnails: Boolean,
    gridColumns: Int,
    fontPadMult: Float,
    iconScaleLevel: Int = 3,
    iconBgMode: String = SettingsStore.ICON_BG_MODE_ICON_TILE,
    iconBgColorStr: String = SettingsStore.ICON_BG_COLOR_SURFACE,
    iconAspectRatioLevel: Int = 3,
    imageThumbnailScale: String = SettingsStore.IMAGE_THUMBNAIL_CROP,
    onNodeTap: () -> Unit,
    onRowLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Background and border for selected state
    val selectedBorder = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    // Tile color mapping
    val tileBgColor = when (iconBgColorStr) {
        SettingsStore.ICON_BG_COLOR_PRIMARY -> MaterialTheme.colorScheme.primaryContainer
        SettingsStore.ICON_BG_COLOR_SECONDARY -> MaterialTheme.colorScheme.secondaryContainer
        SettingsStore.ICON_BG_COLOR_TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer
        SettingsStore.ICON_BG_COLOR_DARK -> Color(0xFF1E293B)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f)
    }

    // Container background distribution based on tile mode
    val outerBg = when (iconBgMode) {
        SettingsStore.ICON_BG_MODE_CARD -> tileBgColor
        else -> Color.Transparent
    }
    
    val topTileBg = when (iconBgMode) {
        SettingsStore.ICON_BG_MODE_ICON_TILE, SettingsStore.ICON_BG_MODE_SPLIT -> tileBgColor
        else -> Color.Transparent
    }

    val textPillBg = when (iconBgMode) {
        SettingsStore.ICON_BG_MODE_SPLIT -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        else -> Color.Transparent
    }

    // Allocated height for upper icon tile container (Levels 1 to 6)
    val tileAspectRatio = 1.55f - ((iconAspectRatioLevel - 1) * 0.14f)

    // Base fill fraction for bitmap thumbnails (0.50f at Level 1 to 1.00f at Level 6)
    val imageFillFraction = (0.50f + ((iconScaleLevel - 1) * 0.10f)).coerceIn(0.40f, 1.00f)

    // Optical compensation for vector icons (Folders & Files):
    val vectorIconScale = imageFillFraction * 1.35f

    // Responsive font size
    val baseFontSize = when {
        gridColumns == 1 -> 15f
        gridColumns == 2 -> 14f
        gridColumns == 3 -> 13f
        gridColumns == 4 -> 12f
        gridColumns == 5 -> 11f
        else -> 10f
    }
    val fontSize = (baseFontSize * fontPadMult).sp

    val thumbnailContentScale = if (imageThumbnailScale == SettingsStore.IMAGE_THUMBNAIL_FIT) ContentScale.Fit else ContentScale.Crop

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(outerBg)
            .then(
                if (isSelected) Modifier.border(2.dp, selectedBorder, RoundedCornerShape(12.dp))
                else Modifier
            )
            .combinedClickable(
                onClick = onNodeTap,
                onLongClick = onRowLongClick
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Upper Icon / Thumbnail Tile Box
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(tileAspectRatio)
                    .clip(RoundedCornerShape(10.dp))
                    .background(topTileBg)
            ) {
                if (showImageThumbnails && node.mimeType.startsWith("image/")) {
                    val context = LocalContext.current
                    // Downscale decoded thumbnail size to 256x256 to drastically reduce CPU/GPU memory & battery drain
                    val imageRequest = remember(node.uri, node.modifiedTimestamp) {
                        ImageRequest.Builder(context)
                            .data(node.uri)
                            .size(256, 256)
                            .memoryCacheKey("${node.uri}_${node.modifiedTimestamp}")
                            .diskCacheKey("${node.uri}_${node.modifiedTimestamp}")
                            .crossfade(false)
                            .build()
                    }

                    AsyncImage(
                        model = imageRequest,
                        contentDescription = node.name,
                        contentScale = thumbnailContentScale,
                        modifier = Modifier
                            .fillMaxSize(imageFillFraction)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Icon(
                        imageVector = if (node.isDirectory) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                        contentDescription = null,
                        tint = if (node.isDirectory) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .fillMaxSize(vectorIconScale.coerceAtMost(1.0f))
                            .then(
                                if (vectorIconScale > 1.0f) Modifier.scale(vectorIconScale)
                                else Modifier
                            )
                    )
                }

                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onNodeTap() },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.TopStart)
                            .padding(2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // File / Folder Text Label
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(textPillBg)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = node.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = fontSize,
                        fontWeight = FontWeight.Medium,
                        lineHeight = fontSize * 1.2f
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
