package com.rustyranger.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rustyranger.app.ui.screens.browser.VfsBrowserScreen
import com.rustyranger.app.ui.screens.SettingsScreen
import com.rustyranger.app.ui.theme.RustyRangerTheme
import com.rustyranger.app.util.SettingsStore
import com.rustyranger.app.util.StoragePermissionHelper

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Check & request All Files Access on Android 11+
        if (!StoragePermissionHelper.hasAllFilesPermission(this)) {
            StoragePermissionHelper.requestAllFilesPermission(this)
        }

        val app = application as RustyRangerApp

        setContent {
            val uiSettings by app.settingsStore.uiSettingsStateFlow.collectAsState()
            val themeMode = uiSettings?.themeMode ?: SettingsStore.THEME_DARK
            val fontFamily = uiSettings?.fontFamily ?: SettingsStore.FONT_SYSTEM

            RustyRangerTheme(themeMode = themeMode, fontFamily = fontFamily) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "browser") {
                        composable("browser") {
                            VfsBrowserScreen(
                                viewModel = app.viewModel,
                                settingsStore = app.settingsStore
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                settingsStore = app.settingsStore,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
