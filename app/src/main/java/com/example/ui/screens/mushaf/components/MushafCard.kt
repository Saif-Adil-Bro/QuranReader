package com.example.ui.screens.mushaf.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DownloadState
import com.example.data.model.DownloadStatus
import com.example.data.model.MushafStyle

@Composable
fun MushafCard(
    mushaf: MushafStyle,
    downloadStatus: DownloadStatus?,
    onSelect: () -> Unit,
    onDownload: () -> Unit,
    onPause: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                if (downloadStatus?.state == DownloadState.Downloaded) {
                    onSelect()
                } else if (downloadStatus?.state != DownloadState.Downloading) {
                    onDownload()
                }
            },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Premium 3D Book Mockup Preview
            MushafBookCoverPreview(
                mushaf = mushaf,
                isDownloaded = downloadStatus?.state == DownloadState.Downloaded
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = mushaf.nameBengali,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = mushaf.descriptionBengali,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (downloadStatus?.state == DownloadState.Downloading) {
                DownloadProgress(
                    status = downloadStatus,
                    onPause = onPause,
                    onCancel = onCancel
                )
            } else if (downloadStatus?.state == DownloadState.Downloaded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("পড়ুন", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        modifier = Modifier
                            .weight(0.7f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                    ) {
                        Text("মুছুন", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                }
            } else {
                Button(
                    onClick = onDownload,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ডাউনলোড করুন (~${mushaf.fileSizeMB} MB)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                
                if ((downloadStatus?.downloadedPages ?: 0) > 0) {
                    Text(
                        text = "আংশিক ডাউনলোড হয়েছে (${downloadStatus?.downloadedPages} পৃষ্ঠা)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MushafBookCoverPreview(
    mushaf: MushafStyle,
    isDownloaded: Boolean
) {
    // Determine gradient based on Mushaf ID for premium physical book look
    val coverGradient = when (mushaf.id) {
        "madani" -> Brush.verticalGradient(
            listOf(Color(0xFF0F5132), Color(0xFF052C16)) // Emerald to Deep Forest Green
        )
        "makkah" -> Brush.verticalGradient(
            listOf(Color(0xFF1A1A1A), Color(0xFF0A0A0A)) // Rich Charcoal to Jet Black
        )
        "indopak" -> Brush.verticalGradient(
            listOf(Color(0xFF78350F), Color(0xFF451A03)) // Warm Sienna to Deep Chocolate Brown
        )
        else -> Brush.verticalGradient(
            listOf(Color(0xFF0F4C81), Color(0xFF07223C)) // Royal Navy to Midnight Blue
        )
    }

    val goldColor = Color(0xFFD4AF37) // Majestic Metallic Gold

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(coverGradient)
    ) {
        // Intricate Gold-Foiled Double Border Accent
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .border(2.dp, goldColor.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                .padding(3.dp)
                .border(0.7.dp, goldColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
        )

        // Hanging Silk Bookmark Ribbon
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 24.dp)
                .width(14.dp)
                .height(48.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFDC2626), Color(0xFF991B1B)) // Crimson red silk ribbon
                    ),
                    shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
                )
                .border(0.5.dp, goldColor.copy(alpha = 0.3f), RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
        )

        // Floating Framed Interactive Page Preview
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(130.dp)
                .height(190.dp)
                .rotate(-3f) // Slight 3D book slant
                .shadow(12.dp, RoundedCornerShape(8.dp))
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(2.dp, goldColor.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            // Elegant background pattern behind page loader
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFAF6EB), RoundedCornerShape(4.dp)), // Soft cream page color
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = mushaf.thumbnailUrl,
                    contentDescription = "Page Preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Fallback elegant calligraphy watermark / emblem
                IslamicEmblem(
                    modifier = Modifier.size(54.dp),
                    color = goldColor.copy(alpha = 0.15f)
                )
            }
        }

        // Mini Badge for selected Mushaf on the book spine or corner
        if (isDownloaded) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(20.dp)
                    .shadow(4.dp, RoundedCornerShape(8.dp))
                    .background(Color(0xFF10B981), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "সংরক্ষিত",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun IslamicEmblem(modifier: Modifier = Modifier, color: Color) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Outer rotated square (8-pointed star shape base)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, color, RoundedCornerShape(6.dp))
                .rotate(45f)
                .border(1.dp, color, RoundedCornerShape(6.dp))
        )
        // Center text symbol
        Text(
            text = "القرآن",
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
