package com.numbux.mdm

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.numbux.mdm.ui.theme.NumbuXTheme
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL



class MainActivity : ComponentActivity() {

    companion object {
        private const val REQUEST_PROVISION_MANAGED_PROFILE = 1
    }

    private fun startWorkProfileProvisioning() {
        val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)
        intent.putExtra(
            DevicePolicyManager.EXTRA_DEVICE_ADMIN,
            ComponentName(this, MyDeviceAdminReceiver::class.java)
        )
        intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_SKIP_ENCRYPTION, true)

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_PROVISION_MANAGED_PROFILE)
        } else {
            Toast.makeText(this, "Provisioning not supported on this device", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NumbuXTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        // 🚀 Trigger provisioning if not yet done
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)
        if (!dpm.isProfileOwnerApp(packageName)) {
            startWorkProfileProvisioning()
            return // ⛔ Stop further setup until work profile is created
        }

        val deviceId = "your_device_id"
        registerDevice(deviceId)
        checkCommands(deviceId)
        checkRemoteCommandLoop()
    }

    private fun registerDevice(deviceId: String) {
        Thread {
            try {
                val url = URL("http://192.168.1.118:5000/register") // ✅ Set this to your server
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val json = JSONObject()
                json.put("device_id", deviceId)

                val os: OutputStream = conn.outputStream
                os.write(json.toString().toByteArray())
                os.flush()
                os.close()

                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                var response: String?
                while (reader.readLine().also { response = it } != null) {
                    Log.d("MDM", response!!)
                }
                reader.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun checkCommands(deviceId: String) {
        Thread {
            try {
                val url = URL("http://192.168.1.118:5000/commands/$deviceId")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                var response: String?
                while (reader.readLine().also { response = it } != null) {
                    if (response?.contains("lock") == true) {
                        lockWorkProfile()
                    }
                }
                reader.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun lockWorkProfile() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)
        dpm.lockNow()
    }

    private fun checkRemoteCommandLoop() {
        Thread {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val admin = ComponentName(this, MyDeviceAdminReceiver::class.java)
            val serverUrl = "https://65c9-79-148-204-218.ngrok-free.app/command"

            while (true) {
                try {
                    val url = URL(serverUrl)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = reader.readLine()
                    reader.close()

                    Log.d("MDM", "🛰️ Server response: $response")

                    val json = JSONObject(response)
                    val command = json.getString("command")

                    if (command.equals("lock", ignoreCase = true)) {
                        if (dpm.isAdminActive(admin)) {
                            dpm.setProfileEnabled(admin) // Disables work profile
                        }
                    } else if (command.equals("unlock", ignoreCase = true)) {
                        runOnUiThread {
                            Toast.makeText(
                                this,
                                "⚠️ Cannot unlock from inside the work profile. Please enable it manually.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    Thread.sleep(5000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NumbuXTheme {
        Greeting("Android")
    }
}
