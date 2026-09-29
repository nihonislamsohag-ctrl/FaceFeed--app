package com.example.model

enum class ReactionType(
    val emoji: String,
    val bengaliName: String,
    val englishName: String,
    val colorHex: Long
) {
    LIKE("👍", "লাইক", "Like", 0xFF1877F2),
    DISLIKE("👎", "ডিসলাইক", "Dislike", 0xFF65676B),
    OK("👌", "ওকে", "OK", 0xFF00A400),
    LOVE("❤️", "লাভ", "Love", 0xFFF3425F),
    CARE("🥰", "কেয়ার", "Care", 0xFFF7B125),
    WOW("😮", "ওয়াও", "Wow", 0xFFF7B125),
    HAHA("😄", "হাহা", "Haha", 0xFFF7B125),
    SAD("😢", "কষ্ট", "Sad", 0xFFF7B125),
    ANGRY("😡", "রাগ", "Angry", 0xFFE94B35),
    HUNDRED("💯", "একশো", "100%", 0xFFFF4500);

    companion object {
        val ALL_REACTIONS = values().toList()
    }
}
