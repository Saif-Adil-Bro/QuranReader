package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.font.Font as ResourceFont
import androidx.compose.ui.text.googlefonts.Font as GoogleFontFile
import com.example.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val uthmanTahaFont = FontFamily(
    ResourceFont(R.font.uthman_taha)
)

val amiriQuranFont = FontFamily(
    ResourceFont(R.font.amiri_quran),
    GoogleFontFile(googleFont = GoogleFont("Amiri Quran"), fontProvider = provider)
)

val amiriFont = FontFamily(
    ResourceFont(R.font.amiri_regular),
    GoogleFontFile(googleFont = GoogleFont("Amiri"), fontProvider = provider)
)

val scheherazadeFont = FontFamily(
    ResourceFont(R.font.scheherazade_new),
    GoogleFontFile(googleFont = GoogleFont("Scheherazade New"), fontProvider = provider)
)

val lateefFont = FontFamily(
    ResourceFont(R.font.lateef_regular),
    GoogleFontFile(googleFont = GoogleFont("Lateef"), fontProvider = provider)
)

val almaraiFont = FontFamily(
    ResourceFont(R.font.almarai_regular),
    GoogleFontFile(googleFont = GoogleFont("Almarai"), fontProvider = provider)
)

val tajawalFont = FontFamily(
    ResourceFont(R.font.tajawal_regular),
    GoogleFontFile(googleFont = GoogleFont("Tajawal"), fontProvider = provider)
)

val solaimanLipiFont = FontFamily(
    ResourceFont(R.font.solaimanlipi, FontWeight.Normal),
    ResourceFont(R.font.solaimanlipi_bold, FontWeight.Bold)
)

val arabicFontsList = listOf(
    "Uthman Taha",
    "Amiri Quran",
    "Amiri",
    "Scheherazade New",
    "Lateef",
    "Almarai",
    "Tajawal"
)

fun getArabicFont(name: String): FontFamily {
    return when (name) {
        "Uthman Taha" -> uthmanTahaFont
        "Amiri" -> amiriFont
        "Scheherazade New" -> scheherazadeFont
        "Lateef" -> lateefFont
        "Almarai" -> almaraiFont
        "Tajawal" -> tajawalFont
        "Amiri Quran" -> amiriQuranFont
        else -> uthmanTahaFont
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
