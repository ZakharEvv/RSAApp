package com.example.rsaproj.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

class ClipboardHelper(private val ctx: Context) {
    private val clipboard = ctx.getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager

    fun copy(text: String, label: String) {
        copyToClipboard(text, label)
    }

    private fun copyToClipboard(text: String, labelText: String) {
        val clip = ClipData.newPlainText(labelText, text)
        clipboard.setPrimaryClip(clip)
    }

    fun getClip(): String? {
        return clipboard.primaryClip
            ?.getItemAt(0)
            ?.text
            ?.toString()
    }
}