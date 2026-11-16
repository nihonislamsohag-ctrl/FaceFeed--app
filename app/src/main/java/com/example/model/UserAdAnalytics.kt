package com.example.model

/**
 * User Ad Analytics Model (ইউজার অ্যাড ভিউ, দৈনিক ইমপ্রেশন ও স্কিপ ট্র্যাকার)
 * ট্র্যাক করে:
 * ১. কোন ব্যক্তি কয়টা অ্যাড দেখলো (totalAdsSeen)
 * ২. কার সামনে কয়টা দৈনিক অ্যাড গেলো (dailyAdsServed)
 * ৩. সে কয়টা অ্যাড সম্পূর্ণ দেখলো (completedAdsWatched)
 * ৪. কয়টা অ্যাড কেটে স্কিপ করলো (skippedAdsCount)
 * ৫. ইউজারের প্রোফাইল আইকন ও ছবি (userAvatar, userName, userId)
 */
data class UserAdAnalytics(
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val userHandle: String = "@user",
    val totalAdsSeen: Int = 0,
    val dailyAdsServed: Int = 0,
    val completedAdsWatched: Int = 0,
    val skippedAdsCount: Int = 0,
    val bannerAdsViewed: Int = 0,
    val interstitialAdsViewed: Int = 0,
    val lastAdSeenTime: String = "সম্প্রতি",
    val recentAdLogs: List<String> = emptyList()
)
