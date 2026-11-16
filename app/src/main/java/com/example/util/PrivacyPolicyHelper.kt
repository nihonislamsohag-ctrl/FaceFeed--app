package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object PrivacyPolicyHelper {
    const val PRIVACY_POLICY_URL =
        "https://www.termsfeed.com/live/cc3fdcba-d730-4765-88e0-06b6331f787f"

    fun openInBrowser(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "ব্রাউজারে লিংক খুলুন: $PRIVACY_POLICY_URL",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
