package com.example.utils

import android.content.Context
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.example.R
import kotlin.math.max

object IslamicCardTemplate {

    data class ShareData(
        val badgeTitle: String,
        val arabicText: String? = null,
        val transliterationText: String? = null,
        val translationText: String,
        val referenceText: String? = null,
        val footerAppName: String = "Quran READER",
        val footerAppSubtext: String = "কুরআন রিডার অ্যাপ থেকে শেয়ারকৃত"
    )

    private fun drawRealisticLeaf(
        canvas: Canvas,
        baseX: Float,
        baseY: Float,
        angleDegrees: Float,
        length: Float = 60f,
        width: Float = 24f
    ) {
        canvas.save()
        canvas.translate(baseX, baseY)
        canvas.rotate(angleDegrees)

        // Leaf shape path
        val leafPath = Path().apply {
            moveTo(0f, 0f)
            cubicTo(length * 0.3f, -width * 0.6f, length * 0.7f, -width * 0.5f, length, 0f)
            cubicTo(length * 0.7f, width * 0.5f, length * 0.3f, width * 0.6f, 0f, 0f)
            close()
        }

        // Gradient fill for leaf (Dark emerald base to lighter vibrant green tip)
        val leafGradient = LinearGradient(
            0f, 0f, length, 0f,
            intArrayOf(
                Color.parseColor("#123D24"),
                Color.parseColor("#2E7D32"),
                Color.parseColor("#52B788")
            ),
            floatArrayOf(0f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )

        val leafPaint = Paint().apply {
            shader = leafGradient
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawPath(leafPath, leafPaint)

        // Center Vein (Light Green/Gold)
        val veinPaint = Paint().apply {
            color = Color.parseColor("#D8F3DC")
            strokeWidth = 2.2f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        canvas.drawLine(0f, 0f, length * 0.88f, 0f, veinPaint)

        // Side veins
        val sideVeinPaint = Paint().apply {
            color = Color.parseColor("#95D5B2")
            strokeWidth = 1.2f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val steps = 3
        for (i in 1..steps) {
            val vx = length * (i * 0.22f)
            canvas.drawLine(vx, 0f, vx + 10f, -width * 0.28f, sideVeinPaint)
            canvas.drawLine(vx, 0f, vx + 10f, width * 0.28f, sideVeinPaint)
        }

        canvas.restore()
    }

    fun generateCardBitmap(context: Context, data: ShareData): Bitmap {
        val width = 1080
        val marginX = 64f
        val cardWidth = width - 2 * marginX // 952px
        val contentPaddingX = 46f
        val contentWidth = (cardWidth - 2 * contentPaddingX).toInt() // 860px

        // Custom fonts
        val arabicFont = try {
            ResourcesCompat.getFont(context, R.font.scheherazade_new) ?: Typeface.DEFAULT
        } catch (e: Exception) {
            Typeface.DEFAULT
        }

        val banglaFont = try {
            ResourcesCompat.getFont(context, R.font.solaimanlipi) ?: Typeface.DEFAULT
        } catch (e: Exception) {
            Typeface.DEFAULT
        }

        val banglaBoldFont = try {
            ResourcesCompat.getFont(context, R.font.solaimanlipi_bold) ?: Typeface.DEFAULT_BOLD
        } catch (e: Exception) {
            Typeface.DEFAULT_BOLD
        }

        // Measure text heights first
        // 1. Arabic Text Layout
        val arabicPaint = TextPaint().apply {
            color = Color.parseColor("#0F291B")
            textSize = 48f
            typeface = arabicFont
            isAntiAlias = true
        }

        val formattedArabic = if (!data.arabicText.isNullOrBlank()) {
            data.arabicText.trim()
        } else ""

        val arabicLayout = if (formattedArabic.isNotEmpty()) {
            StaticLayout.Builder.obtain(formattedArabic, 0, formattedArabic.length, arabicPaint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 0.9f)
                .setIncludePad(true)
                .build()
        } else null

        // 2. Transliteration Layout
        val transliterationPaint = TextPaint().apply {
            color = Color.parseColor("#2E4436")
            textSize = 36f
            typeface = banglaFont
            isAntiAlias = true
        }

        val transliterationLayout = if (!data.transliterationText.isNullOrBlank()) {
            StaticLayout.Builder.obtain(data.transliterationText.trim(), 0, data.transliterationText.trim().length, transliterationPaint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.25f)
                .setIncludePad(true)
                .build()
        } else null

        // 3. Translation Layout
        val translationPaint = TextPaint().apply {
            color = Color.parseColor("#182B1E")
            textSize = 40f
            typeface = banglaFont
            isAntiAlias = true
        }

        val cleanTranslation = try {
            android.text.Html.fromHtml(data.translationText.trim(), android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
        } catch (e: Exception) {
            data.translationText.trim()
        }

        val translationLayout = StaticLayout.Builder.obtain(cleanTranslation, 0, cleanTranslation.length, translationPaint, contentWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1.35f)
            .setIncludePad(true)
            .build()

        // Calculate card height required
        val topPaddingInCard = 110f // Space below arch badge
        var cardInnerContentHeight = topPaddingInCard

        if (arabicLayout != null) {
            cardInnerContentHeight += arabicLayout.height + 35f // Arabic text
            cardInnerContentHeight += 30f // Divider gap
        }

        if (transliterationLayout != null) {
            cardInnerContentHeight += transliterationLayout.height + 25f
            cardInnerContentHeight += 25f // Divider gap
        }

        cardInnerContentHeight += translationLayout.height + 40f

        if (!data.referenceText.isNullOrBlank()) {
            cardInnerContentHeight += 70f // Reference pill
        }

        cardInnerContentHeight += 60f // Bottom padding inside card

        val cardTop = 150f
        val cardBottom = cardTop + max(cardInnerContentHeight, 520f)
        val footerSpace = 250f
        val totalCanvasHeight = (cardBottom + footerSpace).toInt().coerceAtLeast(1080)

        // Create bitmap and canvas
        val bitmap = Bitmap.createBitmap(width, totalCanvasHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // ==========================================
        // 1. OUTER BACKGROUND (Deep Emerald Gradient)
        // ==========================================
        val bgGradient = LinearGradient(
            0f, 0f, 0f, totalCanvasHeight.toFloat(),
            intArrayOf(
                Color.parseColor("#042618"), // Deep Forest Emerald Top
                Color.parseColor("#093E28"), // Rich Emerald Middle
                Color.parseColor("#031D12")  // Midnight Emerald Bottom
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        val bgPaint = Paint().apply { shader = bgGradient }
        canvas.drawRect(0f, 0f, width.toFloat(), totalCanvasHeight.toFloat(), bgPaint)

        // Draw Mosque Silhouette at bottom
        val mosquePaint = Paint().apply {
            color = Color.parseColor("#02140C")
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val mosquePath = Path().apply {
            val base = totalCanvasHeight.toFloat() - 40f
            moveTo(0f, base)
            lineTo(0f, base - 120f)
            // Left Minaret spire
            lineTo(30f, base - 120f)
            lineTo(40f, base - 220f)
            lineTo(50f, base - 120f)
            lineTo(120f, base - 120f)
            // Left Dome
            quadTo(200f, base - 250f, 280f, base - 120f)
            lineTo(800f, base - 120f)
            // Right Dome
            quadTo(880f, base - 250f, 960f, base - 120f)
            lineTo(1030f, base - 120f)
            // Right Minaret spire
            lineTo(1040f, base - 220f)
            lineTo(1050f, base - 120f)
            lineTo(width.toFloat(), base - 120f)
            lineTo(width.toFloat(), base + 40f)
            lineTo(0f, base + 40f)
            close()
        }
        canvas.drawPath(mosquePath, mosquePaint)

        // Draw Hanging Gold Lantern at Top Right
        val lanternX = 940f
        val chainPaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        canvas.drawLine(lanternX, 0f, lanternX, 130f, chainPaint)

        // Lantern Glow
        val lanternGlow = RadialGradient(
            lanternX, 190f, 90f,
            intArrayOf(
                Color.parseColor("#FFFFE0"),
                Color.parseColor("#FFD700"),
                Color.parseColor("#00FFD700")
            ),
            floatArrayOf(0f, 0.4f, 1f),
            Shader.TileMode.CLAMP
        )
        val glowPaint = Paint().apply {
            shader = lanternGlow
            isAntiAlias = true
        }
        canvas.drawCircle(lanternX, 190f, 90f, glowPaint)

        // Lantern Cap & Body
        val lanternGoldPaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        // Top Cap
        val capPath = Path().apply {
            moveTo(lanternX - 25f, 130f)
            lineTo(lanternX + 25f, 130f)
            lineTo(lanternX + 35f, 150f)
            lineTo(lanternX - 35f, 150f)
            close()
        }
        canvas.drawPath(capPath, lanternGoldPaint)

        // Glass Body
        val lanternGlassPath = Path().apply {
            moveTo(lanternX - 35f, 150f)
            lineTo(lanternX + 35f, 150f)
            lineTo(lanternX + 25f, 220f)
            lineTo(lanternX - 25f, 220f)
            close()
        }
        val glassPaint = Paint().apply {
            color = Color.parseColor("#40FFD700")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawPath(lanternGlassPath, glassPaint)
        
        // Lantern Ribs
        canvas.drawLine(lanternX - 35f, 150f, lanternX - 25f, 220f, chainPaint)
        canvas.drawLine(lanternX, 150f, lanternX, 220f, chainPaint)
        canvas.drawLine(lanternX + 35f, 150f, lanternX + 25f, 220f, chainPaint)

        // Lantern Base & Tassel
        val baseRect = RectF(lanternX - 30f, 220f, lanternX + 30f, 232f)
        canvas.drawRoundRect(baseRect, 4f, 4f, lanternGoldPaint)
        canvas.drawLine(lanternX, 232f, lanternX, 255f, chainPaint)

        // ==========================================
        // DRAW ELEGANT LUSH VINE & LEAVES AT TOP LEFT
        // ==========================================
        val stemPaint = Paint().apply {
            color = Color.parseColor("#123D24")
            strokeWidth = 6f
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
        }

        // Main branch stem
        val stemPath = Path().apply {
            moveTo(-20f, -20f)
            cubicTo(30f, 25f, 80f, 50f, 165f, 95f)
        }
        canvas.drawPath(stemPath, stemPaint)

        // Sub-branch stem
        val subStemPath = Path().apply {
            moveTo(55f, 38f)
            cubicTo(85f, 15f, 125f, 10f, 160f, 15f)
        }
        canvas.drawPath(subStemPath, stemPaint)

        // Leaf placements: (baseX, baseY, angleDegrees, length, width)
        val leavesData = listOf(
            listOf(10f, 8f, 30f, 75f, 28f),
            listOf(35f, 25f, 75f, 60f, 24f),
            listOf(55f, 38f, -25f, 80f, 30f),
            listOf(80f, 22f, 15f, 65f, 25f),
            listOf(115f, 14f, -15f, 70f, 26f),
            listOf(155f, 15f, -35f, 55f, 22f),
            listOf(85f, 55f, 80f, 65f, 25f),
            listOf(125f, 75f, 35f, 70f, 27f),
            listOf(165f, 95f, 50f, 60f, 23f)
        )

        for (leaf in leavesData) {
            drawRealisticLeaf(
                canvas,
                leaf[0].toFloat(),
                leaf[1].toFloat(),
                leaf[2].toFloat(),
                leaf[3].toFloat(),
                leaf[4].toFloat()
            )
        }


        // ==========================================
        // 2. INNER IVORY ARCHED CARD (Mihrab Frame)
        // ==========================================
        val cardLeft = marginX
        val cardRight = width - marginX

        // Build Islamic Arch Path
        val cardPath = Path().apply {
            val shoulderY = cardTop + 140f
            moveTo(cardLeft + 36f, cardBottom)
            // Bottom-Left rounded corner
            quadTo(cardLeft, cardBottom, cardLeft, cardBottom - 36f)
            // Left vertical line up to shoulder
            lineTo(cardLeft, shoulderY)
            // Islamic Arch Peak curve to top center (540, cardTop)
            cubicTo(
                cardLeft + 10f, cardTop + 40f,
                540f - 120f, cardTop - 10f,
                540f, cardTop - 35f
            )
            cubicTo(
                540f + 120f, cardTop - 10f,
                cardRight - 10f, cardTop + 40f,
                cardRight, shoulderY
            )
            // Right vertical line down
            lineTo(cardRight, cardBottom - 36f)
            // Bottom-Right rounded corner
            quadTo(cardRight, cardBottom, cardRight - 36f, cardBottom)
            close()
        }

        // Fill Inner Card with Cream Gradient
        val cardBgGradient = LinearGradient(
            0f, cardTop, 0f, cardBottom,
            intArrayOf(
                Color.parseColor("#FFFDF7"), // Cream Light Top
                Color.parseColor("#FAF4E8"), // Warm Cream Middle
                Color.parseColor("#F3ECDC")  // Soft Ivory Bottom
            ),
            floatArrayOf(0f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        val cardBgPaint = Paint().apply {
            shader = cardBgGradient
            isAntiAlias = true
        }
        canvas.drawPath(cardPath, cardBgPaint)

        // Outer Metallic Gold Border
        val goldShader = LinearGradient(
            cardLeft, cardTop, cardRight, cardBottom,
            intArrayOf(
                Color.parseColor("#E5C158"),
                Color.parseColor("#BF953F"),
                Color.parseColor("#FCF6BA"),
                Color.parseColor("#B38728"),
                Color.parseColor("#FBF5B7")
            ),
            floatArrayOf(0f, 0.25f, 0.5f, 0.75f, 1f),
            Shader.TileMode.CLAMP
        )

        val goldBorderPaint = Paint().apply {
            shader = goldShader
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        canvas.drawPath(cardPath, goldBorderPaint)

        // Inner Fine Gold Line
        val innerCardPath = Path().apply {
            val offset = 12f
            val iLeft = cardLeft + offset
            val iRight = cardRight - offset
            val iTop = cardTop + offset
            val iBottom = cardBottom - offset
            val shoulderY = iTop + 130f

            moveTo(iLeft + 30f, iBottom)
            quadTo(iLeft, iBottom, iLeft, iBottom - 30f)
            lineTo(iLeft, shoulderY)
            cubicTo(
                iLeft + 10f, iTop + 40f,
                540f - 110f, iTop - 10f,
                540f, iTop - 32f
            )
            cubicTo(
                540f + 110f, iTop - 10f,
                iRight - 10f, iTop + 40f,
                iRight, shoulderY
            )
            lineTo(iRight, iBottom - 30f)
            quadTo(iRight, iBottom, iRight - 30f, iBottom)
            close()
        }

        val innerGoldPaint = Paint().apply {
            shader = goldShader
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawPath(innerCardPath, innerGoldPaint)

        // Corner Flourishes (Gold Ornaments)
        val ornamentPaint = TextPaint().apply {
            color = Color.parseColor("#C5A059")
            textSize = 32f
            typeface = banglaBoldFont
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        // Top Left & Top Right Shoulder Ornaments
        canvas.drawText("✤", cardLeft + 45f, cardTop + 170f, ornamentPaint)
        canvas.drawText("✤", cardRight - 45f, cardTop + 170f, ornamentPaint)
        // Bottom Left & Bottom Right Ornaments
        canvas.drawText("✤", cardLeft + 45f, cardBottom - 35f, ornamentPaint)
        canvas.drawText("✤", cardRight - 45f, cardBottom - 35f, ornamentPaint)


        // ==========================================
        // 3. HEADER BADGE (Top Center Peak)
        // ==========================================
        val badgeText = data.badgeTitle
        val badgeTextPaint = TextPaint().apply {
            color = Color.parseColor("#FCF6BA")
            textSize = 34f
            typeface = banglaBoldFont
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val badgeTextWidth = badgeTextPaint.measureText(badgeText)
        val badgeWidth = badgeTextWidth + 90f
        val badgeRect = RectF(
            (540f - badgeWidth / 2f),
            (cardTop - 70f),
            (540f + badgeWidth / 2f),
            (cardTop + 10f)
        )

        val badgeBgPaint = Paint().apply {
            color = Color.parseColor("#0A3F29")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val badgeBorderPaint = Paint().apply {
            shader = goldShader
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }

        canvas.drawRoundRect(badgeRect, 35f, 35f, badgeBgPaint)
        canvas.drawRoundRect(badgeRect, 35f, 35f, badgeBorderPaint)
        canvas.drawText(badgeText, 540f, cardTop - 23f, badgeTextPaint)


        // ==========================================
        // 4. DRAW CONTENT INSIDE CARD
        // ==========================================
        var currentY = cardTop + 75f

        // 4a. Arabic Text
        if (arabicLayout != null) {
            canvas.save()
            canvas.translate(marginX + contentPaddingX, currentY)
            arabicLayout.draw(canvas)
            canvas.restore()

            currentY += arabicLayout.height + 25f

            // Gold Ornament Line below Arabic
            val linePaint = Paint().apply {
                shader = goldShader
                strokeWidth = 2f
                style = Paint.Style.STROKE
                isAntiAlias = true
            }
            canvas.drawLine(540f - 180f, currentY, 540f + 180f, currentY, linePaint)
            canvas.drawText("❖", 540f, currentY + 10f, ornamentPaint)
            currentY += 40f
        }

        // 4b. Transliteration / Pronunciation
        if (transliterationLayout != null) {
            canvas.save()
            canvas.translate(marginX + contentPaddingX, currentY)
            transliterationLayout.draw(canvas)
            canvas.restore()

            currentY += transliterationLayout.height + 20f

            val smallDotPaint = TextPaint().apply {
                color = Color.parseColor("#C5A059")
                textSize = 24f
                typeface = banglaFont
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("•• ❖ ••", 540f, currentY + 8f, smallDotPaint)
            currentY += 35f
        }

        // 4c. Bengali Translation
        canvas.save()
        canvas.translate(marginX + contentPaddingX, currentY)
        translationLayout.draw(canvas)
        canvas.restore()

        currentY += translationLayout.height + 35f

        // 4d. Reference Pill (e.g. [ 📖 সূরা ত্বোয়া-হা : ১১৪ ])
        if (!data.referenceText.isNullOrBlank()) {
            val refText = "📖  ${data.referenceText.trim()}"
            val refPaint = TextPaint().apply {
                color = Color.WHITE
                textSize = 32f
                typeface = banglaBoldFont
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }

            val refTextWidth = refPaint.measureText(refText)
            val refPillWidth = refTextWidth + 60f
            val refPillRect = RectF(
                540f - refPillWidth / 2f,
                currentY,
                540f + refPillWidth / 2f,
                currentY + 56f
            )

            val refPillBg = Paint().apply {
                color = Color.parseColor("#0C422D")
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val refPillBorder = Paint().apply {
                shader = goldShader
                style = Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }

            canvas.drawRoundRect(refPillRect, 28f, 28f, refPillBg)
            canvas.drawRoundRect(refPillRect, 28f, 28f, refPillBorder)
            canvas.drawText(refText, 540f, currentY + 38f, refPaint)
        }


        // ==========================================
        // 5. BOTTOM BRANDING & SOCIAL LINKS SECTION
        // ==========================================
        val footerY = cardBottom + 35f

        // Draw App Logo Icon & Name
        val appNamePaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 36f
            typeface = banglaBoldFont
            isAntiAlias = true
        }

        val logoBitmap = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.credit)
                ?: BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher)
        } catch (e: Exception) {
            null
        }

        if (logoBitmap != null) {
            val logoSize = 60f
            val logoTextGap = 16f
            val totalFooterWidth = logoSize + logoTextGap + appNamePaint.measureText(data.footerAppName)
            val footerStartX = (width - totalFooterWidth) / 2f

            // Logo with rounded gold border
            val logoDst = RectF(footerStartX, footerY, footerStartX + logoSize, footerY + logoSize)
            val logoBgRect = RectF(footerStartX - 4f, footerY - 4f, footerStartX + logoSize + 4f, footerY + logoSize + 4f)

            val logoBgPaint = Paint().apply {
                color = Color.parseColor("#0C422D")
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val logoBorderPaint = Paint().apply {
                shader = goldShader
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }

            canvas.drawRoundRect(logoBgRect, 16f, 16f, logoBgPaint)
            canvas.drawRoundRect(logoBgRect, 16f, 16f, logoBorderPaint)

            canvas.drawBitmap(logoBitmap, null, logoDst, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))

            val textX = footerStartX + logoSize + logoTextGap
            canvas.drawText(data.footerAppName, textX, footerY + 40f, appNamePaint)
        } else {
            appNamePaint.textAlign = Paint.Align.CENTER
            canvas.drawText(data.footerAppName, width / 2f, footerY + 40f, appNamePaint)
        }

        // ==========================================
        // SOCIAL CREDITS PILL (FB & Telegram Links)
        // ==========================================
        val socialBarY = footerY + 80f
        val socialTextPaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 28f
            typeface = banglaBoldFont
            isAntiAlias = true
        }

        val fbText = "/MuslimsLibrary"
        val tgText = "/MuslimsLibraryApp"

        val fbTextWidth = socialTextPaint.measureText(fbText)
        val tgTextWidth = socialTextPaint.measureText(tgText)

        val iconSize = 36f
        val iconGap = 12f
        val itemGap = 28f
        val sectionDividerWidth = 24f

        val totalPillWidth = (iconSize + iconGap + fbTextWidth) + itemGap + sectionDividerWidth + itemGap + (iconSize + iconGap + tgTextWidth) + 50f
        val pillHeight = 54f

        val pillStartX = (width - totalPillWidth) / 2f
        val pillRect = RectF(pillStartX, socialBarY, pillStartX + totalPillWidth, socialBarY + pillHeight)

        val pillBgPaint = Paint().apply {
            color = Color.parseColor("#072D1C")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val pillBorderPaint = Paint().apply {
            shader = goldShader
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            isAntiAlias = true
        }

        canvas.drawRoundRect(pillRect, 27f, 27f, pillBgPaint)
        canvas.drawRoundRect(pillRect, 27f, 27f, pillBorderPaint)

        var currentX = pillStartX + 25f

        // Facebook Icon (Blue Circle with white 'f')
        val fbCirclePaint = Paint().apply {
            color = Color.parseColor("#1877F2")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(currentX + iconSize / 2f, socialBarY + pillHeight / 2f, iconSize / 2f, fbCirclePaint)

        val fbFPaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("f", currentX + iconSize / 2f, socialBarY + pillHeight / 2f + 9f, fbFPaint)

        currentX += iconSize + iconGap
        canvas.drawText(fbText, currentX, socialBarY + pillHeight / 2f + 10f, socialTextPaint)

        currentX += fbTextWidth + itemGap

        // Vertical Divider |
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#50FFFFFF")
            strokeWidth = 2f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        canvas.drawLine(currentX + sectionDividerWidth / 2f, socialBarY + 12f, currentX + sectionDividerWidth / 2f, socialBarY + pillHeight - 12f, dividerPaint)

        currentX += sectionDividerWidth + itemGap

        // Telegram Icon (Cyan Circle with white plane)
        val tgCirclePaint = Paint().apply {
            color = Color.parseColor("#229ED9")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(currentX + iconSize / 2f, socialBarY + pillHeight / 2f, iconSize / 2f, tgCirclePaint)

        // Draw Telegram paper plane vector icon
        val tgPlanePath = Path().apply {
            val cx = currentX + iconSize / 2f
            val cy = socialBarY + pillHeight / 2f
            moveTo(cx - 8f, cy + 1f)
            lineTo(cx + 9f, cy - 7f)
            lineTo(cx + 3f, cy + 8f)
            lineTo(cx + 1f, cy + 3f)
            close()
        }
        val tgPlanePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawPath(tgPlanePath, tgPlanePaint)

        currentX += iconSize + iconGap
        canvas.drawText(tgText, currentX, socialBarY + pillHeight / 2f + 10f, socialTextPaint)

        return bitmap
    }
}

