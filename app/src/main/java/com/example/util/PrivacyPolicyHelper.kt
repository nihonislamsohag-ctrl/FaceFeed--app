package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object PrivacyPolicyHelper {
    const val PRIVACY_POLICY_URL =
        "https://www.termsfeed.com/live/0dd2a5b3-46bc-4931-9af1-883a72650b9a"

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
