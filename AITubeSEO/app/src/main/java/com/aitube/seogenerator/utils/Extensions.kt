package com.aitube.seogenerator.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast

fun Context.copyToClipboard(label: String, text: String) {
    try {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(this, "✅ Copied!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(this, "Copy failed. Please try again.", Toast.LENGTH_SHORT).show()
    }
}

fun Context.shareText(text: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    } catch (e: Exception) {
        Toast.makeText(this, "Share failed. No app available.", Toast.LENGTH_SHORT).show()
    }
}

fun View.animateIn(delayMs: Long = 0) {
    alpha = 0f
    translationY = 60f
    animate()
        .alpha(1f)
        .translationY(0f)
        .setStartDelay(delayMs.coerceAtLeast(0))
        .setDuration(420)
        .start()
}

fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }
