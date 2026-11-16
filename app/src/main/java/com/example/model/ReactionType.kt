package com.example.model

enum class ReactionType(
    val emoji: String,
    val bengaliName: String,
    val englishName: String,
    val colorHex: Long
) {
    LIKE("👍", "Like", "Like", 0xFF1877F2),
    DISLIKE("👎", "Dislike", "Dislike", 0xFF65676B),
    OK("👌", "Nice", "Nice", 0xFF00A400),
    LOVE("❤️", "Love", "Love", 0xFFF3425F),
    CARE("🥰", "Adorable", "Adorable", 0xFFF7B125),
    WOW("😮", "Wow", "Wow", 0xFFF7B125),
    HAHA("😄", "Happy", "Happy", 0xFFF7B125),
    SAD("😢", "Sad", "Sad", 0xFFF7B125),
    ANGRY("😡", "Angry", "Angry", 0xFFE94B35),
    HUNDRED("💯", "100", "100", 0xFFFF4500);

    companion object {
        val ALL_REACTIONS = values().toList()
    }
}
