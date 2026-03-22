package com.comicstack.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object MihonLauncher {
    private val MIHON_PACKAGES = listOf("app.mihon", "eu.kanade.tachiyomi")

    fun openInMihon(context: Context, readallcomicsUrl: String) {
        val uri = Uri.parse(readallcomicsUrl)
        // Try each Mihon package directly — don't use resolveActivity,
        // it returns null on Android 11+ even with <queries> declared
        for (pkg in MIHON_PACKAGES) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage(pkg)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            } catch (_: Exception) { }
        }
        // Fallback: browser
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            Toast.makeText(context, "Mihon not found — opened in browser", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open URL", Toast.LENGTH_SHORT).show()
        }
    }
}
