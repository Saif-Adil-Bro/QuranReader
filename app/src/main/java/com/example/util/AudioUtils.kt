package com.example.util

object AudioUtils {
    fun getAudioUrl(qariId: String, ayahNumber: Int): String {
        val bitrate = when (qariId) {
            "ar.abdulbasitmurattal", "ar.abdullahbasfar", "ar.abdurrahmaansudais" -> "192"
            else -> "128"
        }
        return "https://cdn.islamic.network/quran/audio/$bitrate/$qariId/$ayahNumber.mp3"
    }
}
