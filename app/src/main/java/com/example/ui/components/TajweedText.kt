package com.example.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import com.example.ui.theme.TajweedColors

private fun String.toArabicNumerals(): String {
    val englishToArabic = mapOf(
        '0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤',
        '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩'
    )
    return this.map { englishToArabic[it] ?: it }.joinToString("")
}

fun parseTajweedText(raw: String, defaultColor: Color): AnnotatedString {
    return buildAnnotatedString {
        // Preprocess <span class=end>...</span>
        val regexEnd = "<span\\s+class=end>([^<]+)</span>".toRegex()
        val preprocessed = raw.replace(regexEnd) { matchResult ->
            "﴿${matchResult.groupValues[1].toArabicNumerals()}﴾"
        }

        var currentIndex = 0
        while (currentIndex < preprocessed.length) {
            val nextTagStart = preprocessed.indexOf("<", currentIndex)
            if (nextTagStart == -1) {
                append(preprocessed.substring(currentIndex))
                break
            }
            
            if (nextTagStart > currentIndex) {
                append(preprocessed.substring(currentIndex, nextTagStart))
            }
            
            val nextTagEnd = preprocessed.indexOf(">", nextTagStart)
            if (nextTagEnd == -1) {
                append(preprocessed.substring(nextTagStart))
                break
            }
            
            val tag = preprocessed.substring(nextTagStart + 1, nextTagEnd)
            
            if (tag.startsWith("/")) {
                if (tag == "/tajweed" || tag == "/span") {
                    try {
                        pop()
                    } catch (e: Exception) {
                        // ignore if stack is empty
                    }
                }
            } else {
                if (tag.startsWith("tajweed class=") || tag.startsWith("span class=")) {
                    val className = tag.substringAfter("class=").trim('\'', '"')
                    val color = TajweedColors[className] ?: defaultColor
                    pushStyle(SpanStyle(color = color))
                }
            }
            currentIndex = nextTagEnd + 1
        }
    }
}

@Composable
fun TajweedText(
    rawTajweedText: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign? = null
) {
    val annotatedString = parseTajweedText(rawTajweedText, color)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Text(
            text = annotatedString,
            modifier = modifier,
            color = color,
            fontSize = fontSize,
            lineHeight = lineHeight,
            fontFamily = fontFamily,
            textAlign = textAlign
        )
    }
}
