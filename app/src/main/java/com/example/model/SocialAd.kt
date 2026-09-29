package com.example.model

/**
 * Model representing Social Welfare and Public Awareness Advertisements (সামাজিক বিজ্ঞাপন)
 */
data class SocialAd(
    val id: String,
    val title: String,
    val organization: String,
    val slogan: String,
    val description: String,
    val category: String, // রক্তদান, বৃক্ষরোপণ, শিশু শিক্ষা, ক্ষুধামুক্ত, নিরাপদ সড়ক, ইত্যাদি
    val bannerImageUrl: String,
    val ctaText: String, // "স্বেচ্ছায় যোগ দিন", "রক্তদাতা খুঁজুন", "চারা রোপণ করুন"
    val durationSeconds: Int = 5,
    val verifiedBadge: String = "সামাজিক জনকল্যাণ বিজ্ঞাপন 🇧🇩"
)
