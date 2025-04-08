package com.numbux.numbux

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var blockedApps: List<String>
    private val whitelist = listOf(
        "com.android.settings",
        "com.android.systemui",
        this.packageName
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ask for USAGE permission
        if (!PermissionHelper.isUsageStatsPermissionGranted(this)) {
            PermissionHelper.openUsageAccessSettings(this)
            Toast.makeText(this, "Please enable Usage Access permission.", Toast.LENGTH_LONG).show()
            return
        }

        // Ask for overlay permission if not granted
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:$packageName"))
            startActivity(intent)
            Toast.makeText(this, "Grant overlay permission to block apps", Toast.LENGTH_LONG).show()
            return
        }

        blockedApps = getAutoDarkList()
        getSharedPreferences("focus_prefs", MODE_PRIVATE)
            .edit().putStringSet("blocked_apps", blockedApps.toSet()).apply()

        findViewById<Button>(R.id.btnActivateFocus).setOnClickListener {
            if (!isAccessibilityServiceEnabled(this, FocusService::class.java)) {
                Toast.makeText(this, "Please enable Accessibility Service for Focus Mode.", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } else {
                // Start the FocusService explicitly if not already started
                val intent = Intent(this, FocusService::class.java)
                startService(intent)

                Toast.makeText(this, "Focus Mode Activated!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getAutoDarkList(): List<String> {
        val pm = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps.filter {
            pm.getLaunchIntentForPackage(it.packageName) != null &&
                    !whitelist.contains(it.packageName)
        }.map { it.packageName }
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
        val expectedComponent = "${context.packageName}/${service.name}"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices.split(":").any { it.equals(expectedComponent, ignoreCase = true) }
    }
}
