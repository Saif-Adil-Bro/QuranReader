package com.example.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.ShortPost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object PostShareUtil {

    enum class CardTheme(
        val title: String,
        val bgColors: Pair<String, String>,
        val textColor: String,
        val accentColor: String,
        val borderColor: String
    ) {
        EMERALD("ইমারেল্ড গ্রিন", Pair("#07301B", "#145233"), "#FFFFFF", "#FBBF24", "#35FFFFFF"),
        NAVY("রয়েল নাভি ব্লু", Pair("#0F172A", "#1E293B"), "#F8FAFC", "#38BDF8", "#30FFFFFF"),
        WARM_CREAM("ওয়ার্ম ক্রিম", Pair("#FFFBEB", "#FEF3C7"), "#1E293B", "#D97706", "#30D97706"),
        DARK_CHARCOAL("ডার্ক চারকোল", Pair("#121212", "#1E1E1E"), "#E2E8F0", "#10B981", "#20FFFFFF"),
        DEEP_AMBER("ডিপ আম্বার", Pair("#3B1700", "#78350F"), "#FEF3C7", "#FBBF24", "#30FBBF24"),
        MIDNIGHT_PURPLE("পারপল নাইট", Pair("#2E1065", "#4C1D95"), "#F3E8FF", "#C084FC", "#30C084FC"),
        SOFT_TEAL("সফট টিল", Pair("#042F2E", "#115E59"), "#CCFBF1", "#2DD4BF", "#302DD4BF")
    }

    fun buildShareText(post: ShortPost): String {
        return buildString {
            append("✨ ").append(post.category).append(" ✨\n\n")
            append(post.text).append("\n\n")
            if (post.reference.isNotEmpty()) {
                append("সূত্র: ").append(post.reference).append("\n")
            }
            append("\n---\n")
            append("📱 ❝কুরআন রিডার❞ অ্যাপ থেকে শেয়ারকৃত।")
        }
    }

    fun copyToClipboard(context: Context, post: ShortPost) {
        val shareText = buildShareText(post)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Short Post Text", shareText)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "লেখাটি ক্লিপবোর্ডে কপি হয়েছে!", Toast.LENGTH_SHORT).show()
    }

    fun shareAsText(context: Context, post: ShortPost) {
        val shareText = buildShareText(post)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "পোস্ট শেয়ার করুন (টেক্সট)"))
    }

    suspend fun generateCardBitmap(
        context: Context,
        post: ShortPost,
        theme: CardTheme = CardTheme.EMERALD,
        bgImageUrl: String? = null,
        overlayAlpha: Float = 0.70f,
        textAlignName: String = "CENTER", // "LEFT", "CENTER", "RIGHT"
        fontName: String = "SolaimanLipi", // "SolaimanLipi", "Hind Siliguri", "Shorif Shishir Unicode", "Default"
        fontSizeSp: Float = 44f,
        customCategory: String? = null,
        customText: String? = null,
        customRef: String? = null,
        showLogo: Boolean = true,
        showWatermark: Boolean = true
    ): Bitmap = withContext(Dispatchers.IO) {
        val width = 1080
        val margin = 80
        val contentWidth = width - 2 * margin

        val displayCategory: String = customCategory?.takeIf { it.isNotBlank() } ?: post.category
        val displayText: String = customText?.takeIf { it.isNotBlank() } ?: post.text
        val displayRef: String = customRef?.takeIf { it.isNotBlank() } ?: post.reference

        // Load custom font based on selection
        val chosenFont = try {
            when (fontName) {
                "Scheherazade New", "Scheherazade" -> ResourcesCompat.getFont(context, R.font.scheherazade_new)
                "Hind Siliguri" -> ResourcesCompat.getFont(context, R.font.hind_siliguri)
                "Shorif Shishir Unicode", "Shorif Shishir" -> ResourcesCompat.getFont(context, R.font.shorif_shishir)
                "SolaimanLipi" -> ResourcesCompat.getFont(context, R.font.solaimanlipi)
                else -> ResourcesCompat.getFont(context, R.font.solaimanlipi)
            } ?: Typeface.DEFAULT
        } catch (e: Exception) {
            Typeface.DEFAULT
        }

        val chosenBoldFont = try {
            when (fontName) {
                "SolaimanLipi" -> ResourcesCompat.getFont(context, R.font.solaimanlipi_bold) ?: chosenFont
                else -> chosenFont
            }
        } catch (e: Exception) {
            chosenFont
        }

        // Alignments
        val paintAlign = when (textAlignName) {
            "LEFT" -> Paint.Align.LEFT
            "RIGHT" -> Paint.Align.RIGHT
            else -> Paint.Align.CENTER
        }

        val staticLayoutAlign = when (textAlignName) {
            "LEFT" -> Layout.Alignment.ALIGN_NORMAL
            "RIGHT" -> Layout.Alignment.ALIGN_OPPOSITE
            else -> Layout.Alignment.ALIGN_CENTER
        }

        val categoryPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(theme.accentColor)
            textSize = 34f
            typeface = chosenBoldFont
            textAlign = paintAlign
        }

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(theme.textColor)
            textSize = fontSizeSp
            typeface = chosenFont
            textAlign = Paint.Align.LEFT // CRITICAL: StaticLayout requires Align.LEFT; alignment is handled by StaticLayout.Alignment
        }

        val refPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(theme.accentColor)
            textSize = 32f
            typeface = chosenFont
            textAlign = paintAlign
        }

        val creditPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(theme.textColor).run {
                Color.argb(170, Color.red(this), Color.green(this), Color.blue(this))
            }
            textSize = 28f
            typeface = chosenBoldFont
            textAlign = Paint.Align.CENTER
        }

        // Measure text layout
        val textLayout = StaticLayout.Builder.obtain(
            displayText, 0, displayText.length, textPaint, contentWidth
        ).setAlignment(staticLayoutAlign).setLineSpacing(12f, 1.15f).build()

        val categoryText = if (displayCategory.isNotBlank()) "— $displayCategory —" else ""
        val refText = if (displayRef.isNotEmpty()) "— $displayRef —" else ""
        val creditText = "📱 ❝কুরআন রিডার❞ অ্যাপ থেকে সংগৃহীত"

        val categoryHeight = if (categoryText.isNotEmpty()) 50f else 0f
        val gap1 = if (categoryText.isNotEmpty()) 30f else 0f
        val textHeight = textLayout.height.toFloat()
        val gap2 = if (refText.isNotEmpty()) 30f else 0f
        val refHeight = if (refText.isNotEmpty()) 45f else 0f

        val middleContentHeight = categoryHeight + gap1 + textHeight + gap2 + refHeight

        val topHeaderSpace = 130f  // Reserved at top for border and top-left credit logo
        val bottomFooterSpace = 130f // Reserved at bottom for divider line and credit watermark

        // Fixed standard square card height (1080x1080 standard 1:1 format)
        val fixedStandardHeight = 1080
        val requiredHeight = (topHeaderSpace + middleContentHeight + bottomFooterSpace).toInt()
        val finalHeight = requiredHeight.coerceAtLeast(fixedStandardHeight)

        val bitmap = Bitmap.createBitmap(width, finalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Optional background image from URL
        var bgBitmapDrawn = false
        if (!bgImageUrl.isNullOrBlank()) {
            try {
                val input = URL(bgImageUrl).openStream()
                val loadedBg = BitmapFactory.decodeStream(input)
                input.close()
                if (loadedBg != null) {
                    val srcRect = Rect(0, 0, loadedBg.width, loadedBg.height)
                    val dstRect = Rect(0, 0, width, finalHeight)
                    canvas.drawBitmap(loadedBg, srcRect, dstRect, Paint(Paint.FILTER_BITMAP_FLAG))
                    bgBitmapDrawn = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Gradient / Solid Color Overlay
        val shader = LinearGradient(
            0f, 0f, 0f, finalHeight.toFloat(),
            Color.parseColor(theme.bgColors.first),
            Color.parseColor(theme.bgColors.second),
            Shader.TileMode.CLAMP
        )
        val bgPaint = Paint().apply {
            this.shader = shader
            if (bgBitmapDrawn) {
                alpha = (overlayAlpha * 255).toInt().coerceIn(20, 255)
            }
        }
        canvas.drawRect(0f, 0f, width.toFloat(), finalHeight.toFloat(), bgPaint)

        // 3. Rounded Decorative Inner Border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(theme.borderColor)
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRoundRect(RectF(32f, 32f, (width - 32).toFloat(), (finalHeight - 32).toFloat()), 28f, 28f, borderPaint)

        // 4. Top-Left Credit Logo (credit.png) - Conditional
        if (showLogo) {
            try {
                val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.credit)
                if (logoBitmap != null) {
                    val logoSize = 64f
                    val logoLeft = 52f
                    val logoTop = 52f
                    val dstRect = RectF(logoLeft, logoTop, logoLeft + logoSize, logoTop + logoSize)
                    val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                    canvas.drawBitmap(logoBitmap, null, dstRect, logoPaint)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 5. Middle Content Vertical Centering
        val availableMiddleHeight = finalHeight.toFloat() - topHeaderSpace - bottomFooterSpace
        val middleStartY = topHeaderSpace + ((availableMiddleHeight - middleContentHeight) / 2f).coerceAtLeast(0f)
        var currentY = middleStartY + (if (categoryText.isNotEmpty()) 35f else 0f)

        val alignX = when (textAlignName) {
            "LEFT" -> margin.toFloat()
            "RIGHT" -> (width - margin).toFloat()
            else -> (width / 2).toFloat()
        }

        // Category Tag
        if (categoryText.isNotEmpty()) {
            canvas.drawText(categoryText, alignX, currentY, categoryPaint)
            currentY += gap1 + 15f
        }

        // Main Text Layout
        canvas.save()
        canvas.translate(margin.toFloat(), currentY)
        textLayout.draw(canvas)
        canvas.restore()
        currentY += textHeight + gap2

        // Reference
        if (refText.isNotEmpty()) {
            currentY += 15f
            canvas.drawText(refText, alignX, currentY, refPaint)
        }

        // 6. Bottom Credit Watermark & Divider Line - Conditional
        if (showWatermark) {
            val footerDividerY = finalHeight.toFloat() - 110f
            val footerCreditY = finalHeight.toFloat() - 60f

            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(theme.borderColor)
                strokeWidth = 2f
            }
            canvas.drawLine(150f, footerDividerY, (width - 150).toFloat(), footerDividerY, linePaint)

            // Watermark Credit
            canvas.drawText(creditText, (width / 2).toFloat(), footerCreditY, creditPaint)
        }

        bitmap
    }

    suspend fun shareAsImage(
        context: Context,
        post: ShortPost,
        theme: CardTheme = CardTheme.EMERALD,
        bgImageUrl: String? = null,
        overlayAlpha: Float = 0.70f,
        textAlignName: String = "CENTER",
        fontName: String = "SolaimanLipi",
        fontSizeSp: Float = 44f,
        customCategory: String? = null,
        customText: String? = null,
        customRef: String? = null,
        showLogo: Boolean = true,
        showWatermark: Boolean = true
    ) {
        try {
            val bitmap = generateCardBitmap(
                context = context,
                post = post,
                theme = theme,
                bgImageUrl = bgImageUrl,
                overlayAlpha = overlayAlpha,
                textAlignName = textAlignName,
                fontName = fontName,
                fontSizeSp = fontSizeSp,
                customCategory = customCategory,
                customText = customText,
                customRef = customRef,
                showLogo = showLogo,
                showWatermark = showWatermark
            )

            val cacheDir = File(context.cacheDir, "shared_posts")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val file = File(cacheDir, "post_${System.currentTimeMillis()}.png")
            withContext(Dispatchers.IO) {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "ফটো কার্ড শেয়ার করুন"))
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "ফটো কার্ড শেয়ার করতে সমস্যা হয়েছে: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun String?.isNullByBlank(): Boolean = this == null || this.isBlank()
}

