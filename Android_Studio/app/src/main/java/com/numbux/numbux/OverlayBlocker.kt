package com.numbux.numbux

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.view.inputmethod.InputMethodManager
import android.app.AlertDialog
import android.content.DialogInterface
import android.util.Log

object OverlayBlocker {
    private var dialog: AlertDialog? = null

    fun show(context: Context) {
        if (dialog != null && dialog!!.isShowing) return  // Avoid opening multiple dialogs

        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_pin_input, null)

        // Log when trying to show overlay
        Log.d("OverlayBlocker", "Attempting to show overlay")

        builder.setView(view)
        builder.setCancelable(false)  // Prevent dismissing by tapping outside

        val input = view.findViewById<EditText>(R.id.pinInput)
        val btnExit = view.findViewById<Button>(R.id.btnExit)

        // Show the dialog
        dialog = builder.create()

        // Log when dialog is created
        Log.d("OverlayBlocker", "Dialog created")

        dialog?.show()

        // Log when dialog is shown
        Log.d("OverlayBlocker", "Dialog shown")

        // Request focus for the EditText
        input.requestFocus()

        // Log when focus is requested
        Log.d("OverlayBlocker", "Input field focused")

        // Add a delay before showing the keyboard
        val handler = android.os.Handler()
        handler.postDelayed({
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)

            // Log when keyboard is triggered
            Log.d("OverlayBlocker", "Keyboard triggered")
        }, 200) // 200ms delay to ensure UI elements are ready

        btnExit.setOnClickListener {
            val pin = input.text.toString()
            if (PinManager.validatePin(context, pin)) {
                Toast.makeText(context, "Focus Mode Exited", Toast.LENGTH_SHORT).show()
                dialog?.dismiss()
                dialog = null
            } else {
                Toast.makeText(context, "Wrong PIN", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
