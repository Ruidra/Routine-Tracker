package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class RoutineCategory(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val colorHex: Long,
    val iconName: String
) {
    PRIVATE_TUITION(
        id = "private_tuition",
        titleBn = "প্রাইভেট / কোচিং",
        titleEn = "Private Tuition",
        colorHex = 0xFF7C3AED, // Violet
        iconName = "School"
    ),
    COLLEGE(
        id = "college",
        titleBn = "কলেজ / ক্লাস",
        titleEn = "College & Classes",
        colorHex = 0xFF2563EB, // Blue
        iconName = "AccountBalance"
    ),
    SELF_STUDY(
        id = "self_study",
        titleBn = "নিজের পড়ার সময়",
        titleEn = "Self Study",
        colorHex = 0xFF059669, // Emerald
        iconName = "MenuBook"
    ),
    REST(
        id = "rest",
        titleBn = "বিশ্রাম ও অবসর",
        titleEn = "Rest & Leisure",
        colorHex = 0xFFEA580C, // Orange
        iconName = "SelfImprovement"
    ),
    SLEEP(
        id = "sleep",
        titleBn = "ঘুমের সময়",
        titleEn = "Sleep Time",
        colorHex = 0xFF4338CA, // Indigo/Midnight
        iconName = "Bedtime"
    ),
    CUSTOM(
        id = "custom",
        titleBn = "অন্যান্য কাজ",
        titleEn = "Custom / Other",
        colorHex = 0xFF0284C7, // Sky
        iconName = "TaskAlt"
    );

    val composeColor: Color
        get() = Color(colorHex)

    companion object {
        fun fromId(id: String): RoutineCategory {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: CUSTOM
        }
    }
}
