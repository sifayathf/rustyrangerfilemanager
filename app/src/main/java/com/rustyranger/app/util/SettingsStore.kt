package com.rustyranger.app.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

data class UiSettings(
    val viewMode: String = SettingsStore.VIEW_MODE_GRID,
    val showPreviewPane: Boolean = false,
    val gridColumnCount: Int = 4,
    val listDensityLevel: Int = 4,
    val uiFontLevel: Int = 3,
    val uiPaddingLevel: Int = 3,
    val itemSpacingLevel: Int = 4,
    val gridHorizontalSpacingLevel: Int = 4,
    val gridVerticalSpacingLevel: Int = 4,
    val iconScaleLevel: Int = 3,
    val iconBgMode: String = SettingsStore.ICON_BG_MODE_ICON_TILE,
    val iconBgColor: String = SettingsStore.ICON_BG_COLOR_SURFACE,
    val iconAspectRatioLevel: Int = 3,
    val imageThumbnailScale: String = SettingsStore.IMAGE_THUMBNAIL_CROP,
    val settingsWidthLevel: Int = 6,
    val themeMode: String = SettingsStore.THEME_DARK,
    val customBackgroundUri: String = "",
    val showImageThumbnails: Boolean = true,
    val showFileMetadata: Boolean = true,
    val fontFamily: String = SettingsStore.FONT_SYSTEM,
    val profiles: String = "[]"
)

// Extension property to create DataStore instance
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {
    companion object {
        val VIEW_MODE_KEY = stringPreferencesKey("view_mode")
        val SHOW_PREVIEW_PANE_KEY = booleanPreferencesKey("show_preview_pane")
        
        val GRID_COLUMN_COUNT_KEY = intPreferencesKey("grid_column_count")
        val LIST_DENSITY_LEVEL_KEY = intPreferencesKey("list_density_level")
        val UI_FONT_LEVEL_KEY = intPreferencesKey("ui_font_level")
        val UI_PADDING_LEVEL_KEY = intPreferencesKey("ui_padding_level")
        val ITEM_SPACING_LEVEL_KEY = intPreferencesKey("item_spacing_level")
        val GRID_HORIZONTAL_SPACING_KEY = intPreferencesKey("grid_horizontal_spacing_level")
        val GRID_VERTICAL_SPACING_KEY = intPreferencesKey("grid_vertical_spacing_level")
        val ICON_SCALE_LEVEL_KEY = intPreferencesKey("icon_scale_level")
        val ICON_BG_MODE_KEY = stringPreferencesKey("icon_bg_mode")
        val ICON_BG_COLOR_KEY = stringPreferencesKey("icon_bg_color")
        val ICON_ASPECT_RATIO_KEY = intPreferencesKey("icon_aspect_ratio_level")
        val IMAGE_THUMBNAIL_SCALE_KEY = stringPreferencesKey("image_thumbnail_scale")
        val SETTINGS_WIDTH_LEVEL_KEY = intPreferencesKey("settings_width_level")
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val CUSTOM_BACKGROUND_URI_KEY = stringPreferencesKey("custom_background_uri")
        val SHOW_IMAGE_THUMBNAILS_KEY = booleanPreferencesKey("show_image_thumbnails")
        
        val SHOW_FILE_METADATA_KEY = booleanPreferencesKey("show_file_metadata")
        val FONT_FAMILY_KEY = stringPreferencesKey("font_family")
        
        const val VIEW_MODE_TREE = "TREE"
        const val VIEW_MODE_GRID = "GRID"
        
        const val THEME_DARK = "DARK"
        const val THEME_LIGHT = "LIGHT"
        const val THEME_SYSTEM = "SYSTEM"

        const val ICON_BG_MODE_ICON_TILE = "ICON_TILE"
        const val ICON_BG_MODE_CARD = "CARD"
        const val ICON_BG_MODE_SPLIT = "SPLIT"
        const val ICON_BG_MODE_TRANSPARENT = "TRANSPARENT"

        const val ICON_BG_COLOR_SURFACE = "SURFACE"
        const val ICON_BG_COLOR_PRIMARY = "PRIMARY"
        const val ICON_BG_COLOR_SECONDARY = "SECONDARY"
        const val ICON_BG_COLOR_TERTIARY = "TERTIARY"
        const val ICON_BG_COLOR_DARK = "DARK"

        const val IMAGE_THUMBNAIL_CROP = "CROP"
        const val IMAGE_THUMBNAIL_FIT = "FIT"
        
        const val FONT_SYSTEM = "SYSTEM"
        const val FONT_SANS_SERIF = "SANS_SERIF"
        const val FONT_SERIF = "SERIF"
        const val FONT_MONOSPACE = "MONOSPACE"
        const val FONT_CURSIVE = "CURSIVE"
        const val FONT_CASUAL = "CASUAL"
        const val FONT_CONDENSED = "CONDENSED"
        const val FONT_THIN = "THIN"
        const val FONT_MEDIUM = "MEDIUM"
        const val FONT_LIGHT = "LIGHT"
        const val FONT_NOTO = "NOTO"
        const val FONT_ELEGANT = "ELEGANT"
        val PROFILES_KEY = stringPreferencesKey("profiles")
    }

    val uiSettingsStateFlow: StateFlow<UiSettings?> = context.dataStore.data.map { p ->
        UiSettings(
            viewMode = p[VIEW_MODE_KEY] ?: VIEW_MODE_GRID,
            showPreviewPane = p[SHOW_PREVIEW_PANE_KEY] ?: false,
            gridColumnCount = p[GRID_COLUMN_COUNT_KEY] ?: 4,
            listDensityLevel = p[LIST_DENSITY_LEVEL_KEY] ?: 4,
            uiFontLevel = p[UI_FONT_LEVEL_KEY] ?: 3,
            uiPaddingLevel = p[UI_PADDING_LEVEL_KEY] ?: 3,
            itemSpacingLevel = p[ITEM_SPACING_LEVEL_KEY] ?: 4,
            gridHorizontalSpacingLevel = p[GRID_HORIZONTAL_SPACING_KEY] ?: 4,
            gridVerticalSpacingLevel = p[GRID_VERTICAL_SPACING_KEY] ?: 4,
            iconScaleLevel = p[ICON_SCALE_LEVEL_KEY] ?: 3,
            iconBgMode = p[ICON_BG_MODE_KEY] ?: ICON_BG_MODE_ICON_TILE,
            iconBgColor = p[ICON_BG_COLOR_KEY] ?: ICON_BG_COLOR_SURFACE,
            iconAspectRatioLevel = p[ICON_ASPECT_RATIO_KEY] ?: 3,
            imageThumbnailScale = p[IMAGE_THUMBNAIL_SCALE_KEY] ?: IMAGE_THUMBNAIL_CROP,
            settingsWidthLevel = p[SETTINGS_WIDTH_LEVEL_KEY] ?: 6,
            themeMode = p[THEME_MODE_KEY] ?: THEME_DARK,
            customBackgroundUri = p[CUSTOM_BACKGROUND_URI_KEY] ?: "",
            showImageThumbnails = p[SHOW_IMAGE_THUMBNAILS_KEY] ?: true,
            showFileMetadata = p[SHOW_FILE_METADATA_KEY] ?: true,
            fontFamily = p[FONT_FAMILY_KEY] ?: FONT_SYSTEM,
            profiles = p[PROFILES_KEY] ?: "[]"
        )
    }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, null)

    suspend fun setViewMode(mode: String) { context.dataStore.edit { it[VIEW_MODE_KEY] = mode } }
    suspend fun setShowPreviewPane(show: Boolean) { context.dataStore.edit { it[SHOW_PREVIEW_PANE_KEY] = show } }
    suspend fun setGridColumnCount(count: Int) { context.dataStore.edit { it[GRID_COLUMN_COUNT_KEY] = count } }
    suspend fun setListDensityLevel(level: Int) { context.dataStore.edit { it[LIST_DENSITY_LEVEL_KEY] = level } }
    suspend fun setUiFontLevel(level: Int) { context.dataStore.edit { it[UI_FONT_LEVEL_KEY] = level } }
    suspend fun setUiPaddingLevel(level: Int) { context.dataStore.edit { it[UI_PADDING_LEVEL_KEY] = level } }
    suspend fun setItemSpacingLevel(level: Int) { context.dataStore.edit { it[ITEM_SPACING_LEVEL_KEY] = level } }
    suspend fun setGridHorizontalSpacingLevel(level: Int) { context.dataStore.edit { it[GRID_HORIZONTAL_SPACING_KEY] = level } }
    suspend fun setGridVerticalSpacingLevel(level: Int) { context.dataStore.edit { it[GRID_VERTICAL_SPACING_KEY] = level } }
    suspend fun setIconScaleLevel(level: Int) { context.dataStore.edit { it[ICON_SCALE_LEVEL_KEY] = level } }
    suspend fun setIconBgMode(mode: String) { context.dataStore.edit { it[ICON_BG_MODE_KEY] = mode } }
    suspend fun setIconBgColor(colorStr: String) { context.dataStore.edit { it[ICON_BG_COLOR_KEY] = colorStr } }
    suspend fun setIconAspectRatioLevel(level: Int) { context.dataStore.edit { it[ICON_ASPECT_RATIO_KEY] = level } }
    suspend fun setImageThumbnailScale(scaleMode: String) { context.dataStore.edit { it[IMAGE_THUMBNAIL_SCALE_KEY] = scaleMode } }
    suspend fun setSettingsWidthLevel(level: Int) { context.dataStore.edit { it[SETTINGS_WIDTH_LEVEL_KEY] = level } }
    suspend fun setThemeMode(mode: String) { context.dataStore.edit { it[THEME_MODE_KEY] = mode } }
    suspend fun setCustomBackgroundUri(uri: String) { context.dataStore.edit { it[CUSTOM_BACKGROUND_URI_KEY] = uri } }
    suspend fun setShowImageThumbnails(show: Boolean) { context.dataStore.edit { it[SHOW_IMAGE_THUMBNAILS_KEY] = show } }
    suspend fun setShowFileMetadata(show: Boolean) { context.dataStore.edit { it[SHOW_FILE_METADATA_KEY] = show } }
    suspend fun setFontFamily(font: String) { context.dataStore.edit { it[FONT_FAMILY_KEY] = font } }
    suspend fun setProfiles(profilesJson: String) { context.dataStore.edit { it[PROFILES_KEY] = profilesJson } }

    suspend fun resetToDefaults() {
        context.dataStore.edit { prefs ->
            prefs[VIEW_MODE_KEY] = VIEW_MODE_GRID
            prefs[SHOW_PREVIEW_PANE_KEY] = false
            prefs[GRID_COLUMN_COUNT_KEY] = 4
            prefs[LIST_DENSITY_LEVEL_KEY] = 4
            prefs[UI_FONT_LEVEL_KEY] = 3
            prefs[UI_PADDING_LEVEL_KEY] = 3
            prefs[ITEM_SPACING_LEVEL_KEY] = 4
            prefs[GRID_HORIZONTAL_SPACING_KEY] = 4
            prefs[GRID_VERTICAL_SPACING_KEY] = 4
            prefs[ICON_SCALE_LEVEL_KEY] = 3
            prefs[ICON_BG_MODE_KEY] = ICON_BG_MODE_ICON_TILE
            prefs[ICON_BG_COLOR_KEY] = ICON_BG_COLOR_SURFACE
            prefs[ICON_ASPECT_RATIO_KEY] = 3
            prefs[IMAGE_THUMBNAIL_SCALE_KEY] = IMAGE_THUMBNAIL_CROP
            prefs[SETTINGS_WIDTH_LEVEL_KEY] = 6
            prefs[THEME_MODE_KEY] = THEME_SYSTEM
            prefs[SHOW_IMAGE_THUMBNAILS_KEY] = true
            prefs[SHOW_FILE_METADATA_KEY] = true
            prefs[FONT_FAMILY_KEY] = FONT_SYSTEM
        }
    }
}
