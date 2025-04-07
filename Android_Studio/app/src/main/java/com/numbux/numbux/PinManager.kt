package com.numbux.numbux

import android.content.Context

object PinManager {
    private const val PREFS = "focus_prefs"
    private const val KEY_PIN = "user_pin"

    fun savePin(context: Context, pin: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_PIN, pin).apply()
    }

    fun getPin(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_PIN, "1234")!!
    }

    fun validatePin(context: Context, input: String): Boolean {
        return input == getPin(context)
    }
}
