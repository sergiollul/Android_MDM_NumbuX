package com.numbux.numbux

import com.numbux.numbux.OverlayBlocker
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class FocusService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        val prefs = getSharedPreferences("focus_prefs", MODE_PRIVATE)
        val blocked = prefs.getStringSet("blocked_apps", emptySet()) ?: emptySet()

        if (blocked.contains(packageName)) {
            OverlayBlocker.show(this)
        }
    }

    override fun onInterrupt() {
        // Do nothing
    }
}