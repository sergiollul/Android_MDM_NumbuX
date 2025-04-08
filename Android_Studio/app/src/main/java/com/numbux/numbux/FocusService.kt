import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.numbux.numbux.OverlayBlocker


class FocusService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // Log to confirm which app is being detected
        Log.d("FocusService", "Triggering overlay block for app: $packageName")

        val prefs = getSharedPreferences("focus_prefs", MODE_PRIVATE)
        val blocked = prefs.getStringSet("blocked_apps", emptySet()) ?: emptySet()

        if (blocked.contains(packageName)) {
            OverlayBlocker.show(this) // This will show the dialog
        }
    }

    override fun onInterrupt() {
        // Do nothing
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
        val expectedComponent = "${context.packageName}/${service.name}"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val isServiceEnabled = enabledServices.split(":").any { it.equals(expectedComponent, ignoreCase = true) }

        // Add a log here to check if the service is detected properly
        Log.d("FocusService", "Is Accessibility Service Enabled: $isServiceEnabled")

        return isServiceEnabled
    }
}
