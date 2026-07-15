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
        val regex = "<(tajweed|span)\\s+class=([a-zA-Z0-9_]+)>([^<]+)</(?:tajweed|span)>".toRegex()
        var lastIndex = 0
        
        for (match in regex.findAll(raw)) {
            val start = match.range.first
            if (start > lastIndex) {
                append(raw.substring(lastIndex, start))
            }
            
            val tagType = match.groupValues[1]
            val className = match.groupValues[2]
            val content = match.groupValues[3]
            
            if (tagType == "span" && className == "end") {
                append("﴿${content.toArabicNumerals()}﴾")
            } else if (tagType == "tajweed") {
                val color = TajweedColors[className] ?: defaultColor
                withStyle(SpanStyle(color = color)) {
                    append(content)
                }
            } else {
                append(content)
            }
            
            lastIndex = match.range.last + 1
        }
        
        if (lastIndex < raw.length) {
            append(raw.substring(lastIndex))
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
