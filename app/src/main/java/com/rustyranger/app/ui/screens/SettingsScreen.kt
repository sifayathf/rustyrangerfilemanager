package com.rustyranger.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rustyranger.app.util.SettingsStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsStore: SettingsStore,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiSettings by settingsStore.uiSettingsStateFlow.collectAsState()
    
    val viewMode = uiSettings?.viewMode ?: SettingsStore.VIEW_MODE_TREE
    val showPreviewPane = uiSettings?.showPreviewPane ?: false
    val gridColumns = uiSettings?.gridColumnCount ?: 4
    val listDensity = uiSettings?.listDensityLevel ?: 4
    val uiFontLevel = uiSettings?.uiFontLevel ?: 3
    val uiPaddingLevel = uiSettings?.uiPaddingLevel ?: 3
    val itemSpacingLevel = uiSettings?.itemSpacingLevel ?: 4
    val gridHSpacingLevel = uiSettings?.gridHorizontalSpacingLevel ?: 4
    val gridVSpacingLevel = uiSettings?.gridVerticalSpacingLevel ?: 4
    val iconScaleLevel = uiSettings?.iconScaleLevel ?: 3
    val iconBgMode = uiSettings?.iconBgMode ?: SettingsStore.ICON_BG_MODE_ICON_TILE
    val iconBgColor = uiSettings?.iconBgColor ?: SettingsStore.ICON_BG_COLOR_SURFACE
    val iconAspectRatioLevel = uiSettings?.iconAspectRatioLevel ?: 3
    val imageThumbnailScale = uiSettings?.imageThumbnailScale ?: SettingsStore.IMAGE_THUMBNAIL_CROP
    val settingsWidthLevel = uiSettings?.settingsWidthLevel ?: 6
    val themeMode = uiSettings?.themeMode ?: SettingsStore.THEME_SYSTEM
    val showImageThumbnails = uiSettings?.showImageThumbnails ?: true
    val customBackgroundUri = uiSettings?.customBackgroundUri ?: ""
    val showFileMetadata = uiSettings?.showFileMetadata ?: true
    val currentFontFamily = uiSettings?.fontFamily ?: SettingsStore.FONT_SYSTEM
    val profilesStr = uiSettings?.profiles ?: "[]"
    
    val scope = rememberCoroutineScope()
    
    val backgroundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch { settingsStore.setCustomBackgroundUri(uri.toString()) }
        }
    }

    val fontPadMult = 0.8f + ((uiFontLevel - 1) * 0.1f)
    val uiPadMult = 0.8f + ((uiPaddingLevel - 1) * 0.1f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) { innerPadding ->
        val baseTypography = MaterialTheme.typography
        val scaledTypography = Typography(
            displayLarge = baseTypography.displayLarge.copy(fontSize = baseTypography.displayLarge.fontSize * fontPadMult),
            displayMedium = baseTypography.displayMedium.copy(fontSize = baseTypography.displayMedium.fontSize * fontPadMult),
            displaySmall = baseTypography.displaySmall.copy(fontSize = baseTypography.displaySmall.fontSize * fontPadMult),
            headlineLarge = baseTypography.headlineLarge.copy(fontSize = baseTypography.headlineLarge.fontSize * fontPadMult),
            headlineMedium = baseTypography.headlineMedium.copy(fontSize = baseTypography.headlineMedium.fontSize * fontPadMult),
            headlineSmall = baseTypography.headlineSmall.copy(fontSize = baseTypography.headlineSmall.fontSize * fontPadMult),
            titleLarge = baseTypography.titleLarge.copy(fontSize = baseTypography.titleLarge.fontSize * fontPadMult),
            titleMedium = baseTypography.titleMedium.copy(fontSize = baseTypography.titleMedium.fontSize * fontPadMult),
            titleSmall = baseTypography.titleSmall.copy(fontSize = baseTypography.titleSmall.fontSize * fontPadMult),
            bodyLarge = baseTypography.bodyLarge.copy(fontSize = baseTypography.bodyLarge.fontSize * fontPadMult),
            bodyMedium = baseTypography.bodyMedium.copy(fontSize = baseTypography.bodyMedium.fontSize * fontPadMult),
            bodySmall = baseTypography.bodySmall.copy(fontSize = baseTypography.bodySmall.fontSize * fontPadMult),
            labelLarge = baseTypography.labelLarge.copy(fontSize = baseTypography.labelLarge.fontSize * fontPadMult),
            labelMedium = baseTypography.labelMedium.copy(fontSize = baseTypography.labelMedium.fontSize * fontPadMult),
            labelSmall = baseTypography.labelSmall.copy(fontSize = baseTypography.labelSmall.fontSize * fontPadMult)
        )
        
        MaterialTheme(typography = scaledTypography) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding((16f * uiPadMult).dp),
                verticalArrangement = Arrangement.spacedBy((24f * uiPadMult).dp)
            ) {
            // View Mode Settings
            Column {
                Text("View Mode", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height((8f * uiPadMult).dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = viewMode == SettingsStore.VIEW_MODE_TREE,
                        onClick = { scope.launch { settingsStore.setViewMode(SettingsStore.VIEW_MODE_TREE) } },
                        modifier = Modifier.scale(fontPadMult)
                    )
                    Text("Tree View (Detailed list)")
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = viewMode == SettingsStore.VIEW_MODE_GRID,
                        onClick = { scope.launch { settingsStore.setViewMode(SettingsStore.VIEW_MODE_GRID) } },
                        modifier = Modifier.scale(fontPadMult)
                    )
                    Text("Grid View (Large icons)")
                }
            }

            val scaleLabels = mapOf(1 to "Tiny", 2 to "Small", 3 to "Normal", 4 to "Large", 5 to "Huge", 6 to "Maximum")
            val spacingLabels = mapOf(1 to "Tight (2dp)", 2 to "Compact (4dp)", 3 to "Normal (6dp)", 4 to "Standard (8dp)", 5 to "Spacious (12dp)", 6 to "Wide (18dp)")

            // Grid Columns
            Column {
                Text("Grid Columns (${gridColumns} cols)", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = gridColumns.toFloat(),
                    onValueChange = { scope.launch { settingsStore.setGridColumnCount(it.toInt()) } },
                    valueRange = 1f..6f,
                    steps = 4
                )
            }

            // Grid Horizontal Spacing
            Column {
                Text("Grid Horizontal Spacing (${spacingLabels[gridHSpacingLevel]})", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = gridHSpacingLevel.toFloat(),
                    onValueChange = { scope.launch { settingsStore.setGridHorizontalSpacingLevel(it.toInt()) } },
                    valueRange = 1f..6f,
                    steps = 4
                )
            }

            // Grid Vertical Spacing
            Column {
                Text("Grid Vertical Spacing (${spacingLabels[gridVSpacingLevel]})", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = gridVSpacingLevel.toFloat(),
                    onValueChange = { scope.launch { settingsStore.setGridVerticalSpacingLevel(it.toInt()) } },
                    valueRange = 1f..6f,
                    steps = 4
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            // Grid Icon Styling & Tile Options
            Column {
                Text("Grid Icon Styling & Tile Options", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height((8f * uiPadMult).dp))

                val iconScaleLabels = mapOf(1 to "Small", 2 to "Compact", 3 to "Standard", 4 to "Large", 5 to "Huge", 6 to "Maximum")
                val blockHeightLabels = mapOf(1 to "Compact Block", 2 to "Medium Block", 3 to "Standard Block", 4 to "Tall Block", 5 to "Extra Tall", 6 to "Maximum Block")

                // Icon Size Ratio
                Column {
                    Text("Icon Size (Ratio vs Text): ${iconScaleLabels[iconScaleLevel]}", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = iconScaleLevel.toFloat(),
                        onValueChange = { scope.launch { settingsStore.setIconScaleLevel(it.toInt()) } },
                        valueRange = 1f..6f,
                        steps = 4
                    )
                }

                // Icon Block Allocated Height
                Column {
                    Text("Icon Block Allocated Height: ${blockHeightLabels[iconAspectRatioLevel]}", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = iconAspectRatioLevel.toFloat(),
                        onValueChange = { scope.launch { settingsStore.setIconAspectRatioLevel(it.toInt()) } },
                        valueRange = 1f..6f,
                        steps = 4
                    )
                }

                Spacer(modifier = Modifier.height((8f * uiPadMult).dp))

                // Icon Background Tile Mode
                Text("Icon Background Tile Mode", style = MaterialTheme.typography.bodyMedium)
                val tileModes = listOf(
                    SettingsStore.ICON_BG_MODE_ICON_TILE to "Icon Tile (Default & Recommended)",
                    SettingsStore.ICON_BG_MODE_CARD to "Full Card",
                    SettingsStore.ICON_BG_MODE_SPLIT to "Split Tile",
                    SettingsStore.ICON_BG_MODE_TRANSPARENT to "Flat / None"
                )
                Column {
                    tileModes.forEach { (modeKey, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = iconBgMode == modeKey,
                                onClick = { scope.launch { settingsStore.setIconBgMode(modeKey) } },
                                modifier = Modifier.scale(fontPadMult)
                            )
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height((8f * uiPadMult).dp))

                // Icon Tile Tint Color
                Text("Icon Tile Tint Color", style = MaterialTheme.typography.bodyMedium)
                val tintColors = listOf(
                    SettingsStore.ICON_BG_COLOR_SURFACE to "Surface (Neutral)",
                    SettingsStore.ICON_BG_COLOR_PRIMARY to "Primary Accent",
                    SettingsStore.ICON_BG_COLOR_SECONDARY to "Secondary Accent",
                    SettingsStore.ICON_BG_COLOR_TERTIARY to "Tertiary Accent",
                    SettingsStore.ICON_BG_COLOR_DARK to "Dark Box"
                )
                Column {
                    tintColors.forEach { (colorKey, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = iconBgColor == colorKey,
                                onClick = { scope.launch { settingsStore.setIconBgColor(colorKey) } },
                                modifier = Modifier.scale(fontPadMult)
                            )
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height((8f * uiPadMult).dp))

                // Image Thumbnail Fit Mode
                Text("Image Thumbnail Fit Mode", style = MaterialTheme.typography.bodyMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf(
                        SettingsStore.IMAGE_THUMBNAIL_CROP to "Fill Tile (Crop)",
                        SettingsStore.IMAGE_THUMBNAIL_FIT to "Fit Entire Image"
                    ).forEach { (modeKey, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = imageThumbnailScale == modeKey,
                                onClick = { scope.launch { settingsStore.setImageThumbnailScale(modeKey) } },
                                modifier = Modifier.scale(fontPadMult)
                            )
                            Text(label, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            // List Density
            Column {
                Text("List View Density", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = listDensity.toFloat(),
                    onValueChange = { scope.launch { settingsStore.setListDensityLevel(it.toInt()) } },
                    valueRange = 1f..6f,
                    steps = 4
                )
            }

            // List Item Spacing
            Column {
                Text("List Item Spacing", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = itemSpacingLevel.toFloat(),
                    onValueChange = { scope.launch { settingsStore.setItemSpacingLevel(it.toInt()) } },
                    valueRange = 1f..6f,
                    steps = 4
                )
            }

            // UI Font Size
            Column {
                Text("Global Font Size (${scaleLabels[uiFontLevel]})", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = uiFontLevel.toFloat(),
                    onValueChange = { scope.launch { settingsStore.setUiFontLevel(it.toInt()) } },
                    valueRange = 1f..6f,
                    steps = 4
                )
            }

            // UI Padding
            Column {
                Text("Global Padding & Spacing (${scaleLabels[uiPaddingLevel]})", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = uiPaddingLevel.toFloat(),
                    onValueChange = { scope.launch { settingsStore.setUiPaddingLevel(it.toInt()) } },
                    valueRange = 1f..6f,
                    steps = 4
                )
            }

            // Settings Menu Width
            Column {
                Text("Settings Menu Width", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = settingsWidthLevel.toFloat(),
                    onValueChange = { scope.launch { settingsStore.setSettingsWidthLevel(it.toInt()) } },
                    valueRange = 1f..6f,
                    steps = 4
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            // Appearance Settings
            Column {
                Text("Appearance", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height((8f * uiPadMult).dp))
                
                Text("Theme Mode", style = MaterialTheme.typography.bodyMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("DARK", "LIGHT", "SYSTEM").forEach { mode ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = { scope.launch { settingsStore.setThemeMode(mode) } },
                                modifier = Modifier.scale(fontPadMult)
                            )
                            Text(mode.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }

                Spacer(modifier = Modifier.height((16f * uiPadMult).dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Custom Background")
                    Button(onClick = { backgroundPickerLauncher.launch("image/*") }) {
                        Text("Select Image")
                    }
                }
                if (customBackgroundUri.isNotBlank()) {
                    TextButton(onClick = { scope.launch { settingsStore.setCustomBackgroundUri("") } }) {
                        Text("Remove Background", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            // File Options
            Column(verticalArrangement = Arrangement.spacedBy((16f * uiPadMult).dp)) {
                Text("File Browser Options", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show Image Thumbnails", style = MaterialTheme.typography.bodyMedium)
                        Text("Display preview of images in file browser", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = showImageThumbnails,
                        onCheckedChange = { scope.launch { settingsStore.setShowImageThumbnails(it) } },
                        modifier = Modifier.scale(fontPadMult)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show Preview Pane", style = MaterialTheme.typography.bodyMedium)
                        Text("Display file preview at bottom when selected", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = showPreviewPane,
                        onCheckedChange = { scope.launch { settingsStore.setShowPreviewPane(it) } },
                        modifier = Modifier.scale(fontPadMult)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show File Metadata", style = MaterialTheme.typography.bodyMedium)
                        Text("Show size and date in tree list view", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = showFileMetadata,
                        onCheckedChange = { scope.launch { settingsStore.setShowFileMetadata(it) } },
                        modifier = Modifier.scale(fontPadMult)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            // Typography Settings
            Column {
                Text("Typography", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height((8f * uiPadMult).dp))
                
                Text("Font Family", style = MaterialTheme.typography.bodyMedium)
                val fonts = listOf(
                    SettingsStore.FONT_SYSTEM to "System",
                    SettingsStore.FONT_SANS_SERIF to "Sans Serif",
                    SettingsStore.FONT_SERIF to "Serif",
                    SettingsStore.FONT_MONOSPACE to "Mono",
                    SettingsStore.FONT_CURSIVE to "Cursive",
                    SettingsStore.FONT_CASUAL to "Casual",
                    SettingsStore.FONT_CONDENSED to "Condensed",
                    SettingsStore.FONT_THIN to "Thin",
                    SettingsStore.FONT_MEDIUM to "Medium",
                    SettingsStore.FONT_LIGHT to "Light",
                    SettingsStore.FONT_NOTO to "Noto Sans",
                    SettingsStore.FONT_ELEGANT to "Elegant"
                )
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    fonts.chunked(3).forEach { rowFonts ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            rowFonts.forEach { (key, label) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    RadioButton(
                                        selected = currentFontFamily == key,
                                        onClick = { scope.launch { settingsStore.setFontFamily(key) } },
                                        modifier = Modifier.scale(fontPadMult).size(28.dp)
                                    )
                                    Text(label, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            // Advanced Profiles
            
            var newProfileName by remember { mutableStateOf("") }
            val profilesList = remember(profilesStr) {
                try {
                    val array = org.json.JSONArray(profilesStr)
                    List(array.length()) { i -> 
                        val obj = array.getJSONObject(i)
                        obj.getString("name") to obj
                    }
                } catch (e: Exception) { emptyList<Pair<String, org.json.JSONObject>>() }
            }

            Column {
                Text("Advanced Profiles", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height((8f * uiPadMult).dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        label = { Text("Profile Name") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                    Button(onClick = {
                        if (newProfileName.isNotBlank()) {
                            scope.launch {
                                val newArray = org.json.JSONArray(profilesStr)
                                val newObj = org.json.JSONObject().apply {
                                    put("name", newProfileName as Any)
                                    put("gridColumns", gridColumns as Any)
                                    put("fontFamily", currentFontFamily as Any)
                                    put("viewMode", viewMode as Any)
                                    put("iconScaleLevel", iconScaleLevel as Any)
                                    put("iconBgMode", iconBgMode as Any)
                                    put("iconBgColor", iconBgColor as Any)
                                    put("iconAspectRatioLevel", iconAspectRatioLevel as Any)
                                }
                                newArray.put(newObj)
                                settingsStore.setProfiles(newArray.toString())
                                newProfileName = ""
                            }
                        }
                    }) {
                        Text("Save")
                    }
                }
                
                Spacer(modifier = Modifier.height((16f * uiPadMult).dp))
                
                // Default Profile
                OutlinedButton(
                    onClick = { scope.launch { settingsStore.resetToDefaults() } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Default Profile")
                }
                
                for ((name, obj) in profilesList) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = (4f * uiPadMult).dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name, style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = {
                            scope.launch {
                                if (obj.has("gridColumns")) settingsStore.setGridColumnCount(obj.getInt("gridColumns"))
                                if (obj.has("fontFamily")) settingsStore.setFontFamily(obj.getString("fontFamily"))
                                if (obj.has("viewMode")) settingsStore.setViewMode(obj.getString("viewMode"))
                                if (obj.has("iconScaleLevel")) settingsStore.setIconScaleLevel(obj.getInt("iconScaleLevel"))
                                if (obj.has("iconBgMode")) settingsStore.setIconBgMode(obj.getString("iconBgMode"))
                                if (obj.has("iconBgColor")) settingsStore.setIconBgColor(obj.getString("iconBgColor"))
                                if (obj.has("iconAspectRatioLevel")) settingsStore.setIconAspectRatioLevel(obj.getInt("iconAspectRatioLevel"))
                            }
                        }) {
                            Text("Apply")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { scope.launch { settingsStore.resetToDefaults() } },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset All Settings To Default")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        }
    }
}
