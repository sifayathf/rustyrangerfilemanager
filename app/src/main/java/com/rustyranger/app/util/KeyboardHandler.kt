package com.rustyranger.app.util

import android.view.KeyEvent

class KeyboardHandler(
    private val onNavigateUp: () -> Unit,
    private val onNavigateDown: () -> Unit,
    private val onEnter: () -> Unit,
    private val onDelete: () -> Unit,
    private val onSelectAll: () -> Unit,
    private val onCopy: () -> Unit,
    private val onPaste: () -> Unit
) {
    fun handleKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) return false

        val isCtrlPressed = event.isCtrlPressed

        return when (event.keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                onNavigateUp()
                true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                onNavigateDown()
                true
            }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER -> {
                onEnter()
                true
            }
            KeyEvent.KEYCODE_FORWARD_DEL, KeyEvent.KEYCODE_DEL -> {
                onDelete()
                true
            }
            KeyEvent.KEYCODE_A -> {
                if (isCtrlPressed) {
                    onSelectAll()
                    true
                } else false
            }
            KeyEvent.KEYCODE_C -> {
                if (isCtrlPressed) {
                    onCopy()
                    true
                } else false
            }
            KeyEvent.KEYCODE_V -> {
                if (isCtrlPressed) {
                    onPaste()
                    true
                } else false
            }
            else -> false
        }
    }
}
