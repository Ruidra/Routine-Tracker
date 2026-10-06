package com.example.data.model

enum class SoundPreference(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val toneType: Int // ToneGenerator tone or sound identifier
) {
    DEFAULT(
        id = "default",
        titleBn = "ডিফল্ট সাউন্ড",
        titleEn = "System Default",
        toneType = 24 // TONE_PROP_BEEP
    ),
    GENTLE_CHIME(
        id = "gentle_chime",
        titleBn = "শান্ত ঘণ্টা (Gentle Chime)",
        titleEn = "Gentle Chime",
        toneType = 28 // TONE_PROP_ACK
    ),
    DIGITAL_ALARM(
        id = "digital_alarm",
        titleBn = "ডিজিটাল অ্যালার্ম (Alarm Beep)",
        titleEn = "Digital Alarm",
        toneType = 25 // TONE_PROP_BEEP2
    ),
    CALM_HARP(
        id = "calm_harp",
        titleBn = "মিষ্টি সুর (Calm Harp)",
        titleEn = "Calm Tone",
        toneType = 94 // TONE_CDMA_ALERT_NETWORK_LITE
    ),
    ENERGETIC_BELL(
        id = "energetic_bell",
        titleBn = "উজ্জ্বল বেল (Energetic Bell)",
        titleEn = "Energetic Bell",
        toneType = 42 // TONE_DTMF_P
    ),
    SILENT(
        id = "silent",
        titleBn = "শব্দহীন (শুধুমাত্র ভাইব্রেশন)",
        titleEn = "Silent (Vibrate only)",
        toneType = -1
    );

    companion object {
        fun fromId(id: String): SoundPreference {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
        }
    }
}
