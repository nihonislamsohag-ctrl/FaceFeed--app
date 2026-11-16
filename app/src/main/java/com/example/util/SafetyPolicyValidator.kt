package com.example.util

/**
 * Community Safety Policy Validator for FaceFeed & Right Manager Fida.
 * Strictly prohibits:
 * 1. Drugs & Narcotics (নেশা ও মাদকজাতীয় যেকোনো পোস্ট বা শেয়ার)
 * 2. Gambling, Betting & Casino Games/Links (জুয়া, বাজি বা জুয়ার গেমস ও লিংক)
 */
object SafetyPolicyValidator {

    private val drugKeywords = listOf(
        "নেশা", "মাদক", "গাঁজা", "গাঁজাখোর", "মদ", "মদ্যপান", "ইয়াবা", "ইয়াবা",
        "ফেনসিডিল", "হেরোইন", "ড্রাগস", "ড্রাগ", "চরস", "প্যাথেডিন", "কোকেন",
        "drugs", "drug", "weed", "cannabis", "narcotics", "marijuana",
        "cocaine", "heroin", "meth", "yaba", "alcohol", "beer", "whiskey"
    )

    private val gamblingKeywords = listOf(
        "জুয়া", "জুয়া", "জুয়াড়ি", "বাজি", "বাজিঘর", "ক্যাসিনো", "লটারি",
        "1xbet", "১xbet", "bet365", "melbet", "babu88", "betbuzz", "betbuzz365",
        "crazy time", "casino", "gambling", "betting", "poker", "jackpot baji",
        "bet link", "roulette", "slot machine", "বিকাশ বাজি", "নগদ বাজি",
        "জুয়ার লিংক", "জুয়ার গেমস", "বাজি ধরার লিংক"
    )

    data class ValidationResult(
        val isAllowed: Boolean,
        val violationType: ViolationType = ViolationType.NONE,
        val reasonMessage: String = ""
    )

    enum class ViolationType {
        NONE, DRUGS, GAMBLING
    }

    /**
     * Checks if the text violates community safety policy regarding drugs or gambling.
     */
    fun validateContent(text: String): ValidationResult {
        if (text.isBlank()) return ValidationResult(isAllowed = true)

        val lower = text.lowercase()

        // Check for drugs / narcotics
        for (kw in drugKeywords) {
            if (lower.contains(kw.lowercase())) {
                return ValidationResult(
                    isAllowed = false,
                    violationType = ViolationType.DRUGS,
                    reasonMessage = "⛔ FaceFeed ও Right Manager Fida নিরাপত্তা নীতি অনুযায়ী নেশা ও মাদকজাতীয় কোনো কনটেন্ট পোস্ট, বুস্ট বা শেয়ার করা সম্পূর্ণ নিষিদ্ধ!"
                )
            }
        }

        // Check for gambling / betting
        for (kw in gamblingKeywords) {
            if (lower.contains(kw.lowercase())) {
                return ValidationResult(
                    isAllowed = false,
                    violationType = ViolationType.GAMBLING,
                    reasonMessage = "⛔ FaceFeed ও Right Manager Fida নিরাপত্তা নীতি অনুযায়ী জুয়ার গেমস, বাজি ধরা বা জুয়ার কোনো লিংক পোস্ট, বুস্ট বা শেয়ার করা সম্পূর্ণ নিষিদ্ধ ও দণ্ডনীয়!"
                )
            }
        }

        return ValidationResult(isAllowed = true)
    }
}
