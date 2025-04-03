package com.numbux.mdm

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

class ProvisioningActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ At this point, the system already set your app as Profile Owner
        Toast.makeText(this, "📦 Work profile is ready!", Toast.LENGTH_LONG).show()

        // Optional: launch your main UI
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}