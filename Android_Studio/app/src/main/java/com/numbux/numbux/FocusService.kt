package com.numbux.numbux

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log


class FocusService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // Log the currently running app package name
        Log.d("FocusService", "Currently running app: $packageName")

        val prefs = getSharedPreferences("focus_prefs", MODE_PRIVATE)
        val blocked = prefs.getStringSet("blocked_apps", emptySet()) ?: emptySet()

        // Check if the app is in the blocked list
        if (blocked.contains(packageName)) {
            OverlayBlocker.show(this)  // Show the overlay if the app is blocked
        }
    }

    override fun onInterrupt() {
        // Do nothing
    }
}
