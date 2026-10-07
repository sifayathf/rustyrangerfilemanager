package com.rustyranger.app

import android.app.Application
import com.rustyranger.app.data.ffi.PlatformEngineHandle
import com.rustyranger.app.ui.screens.browser.VfsBrowserViewModel
import com.rustyranger.app.util.SettingsStore

class RustyRangerApp : Application() {
    val engineHandle: PlatformEngineHandle by lazy { PlatformEngineHandle.create() }
    val settingsStore: SettingsStore by lazy { SettingsStore(this) }
    val viewModel: VfsBrowserViewModel by lazy { VfsBrowserViewModel(engineHandle) }

    override fun onCreate() {
        super.onCreate()
    }
}
