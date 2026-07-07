package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontLoadingStrategy
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import com.example.R

val amiriQuranFont = FontFamily(
    Font(R.font.amiri_quran, loadingStrategy = FontLoadingStrategy.OptionalLocal)
)

val amiriFont = FontFamily(
    Font(R.font.amiri_regular, loadingStrategy = FontLoadingStrategy.OptionalLocal)
)

val scheherazadeFont = FontFamily(
    Font(R.font.scheherazade_new, loadingStrategy = FontLoadingStrategy.OptionalLocal)
)

val lateefFont = FontFamily(
    Font(R.font.lateef_regular, loadingStrategy = FontLoadingStrategy.OptionalLocal)
)

val almaraiFont = FontFamily(
    Font(R.font.almarai_regular, loadingStrategy = FontLoadingStrategy.OptionalLocal)
)

val tajawalFont = FontFamily(
    Font(R.font.tajawal_regular, loadingStrategy = FontLoadingStrategy.OptionalLocal)
)

val arabicFontsList = listOf(
    "Amiri Quran",
    "Amiri",
    "Scheherazade New",
    "Lateef",
    "Almarai",
    "Tajawal"
)

fun getArabicFont(name: String): FontFamily {
    return when (name) {
        "Amiri" -> amiriFont
        "Scheherazade New" -> scheherazadeFont
        "Lateef" -> lateefFont
        "Almarai" -> almaraiFont
        "Tajawal" -> tajawalFont
        else -> amiriQuranFont
    }
}

// Set of Material typography styles to start with
val Typography =
  Typography(
    bodyLarge =
      TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
      )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
  )
