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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle

@Composable
fun MushafCard(
    mushaf: MushafStyle,
    downloadStatus: DownloadStatus?,
    isDefault: Boolean = false,
    onSetDefault: () -> Unit = {},
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
                val canRead = downloadStatus?.state == DownloadState.Downloaded || 
                              ((downloadStatus?.progress ?: 0) >= 10 && !mushaf.isPdf)
                if (canRead) {
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
                if (downloadStatus.progress >= 10 && !mushaf.isPdf) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("পড়া শুরু করুন (আংশিক)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
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
                Spacer(modifier = Modifier.height(12.dp))
                if (isDefault) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE0F2FE), RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Default",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ডিফল্ট কুরআন হিসেবে সেট করা",
                            color = Color(0xFF0369A1),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onSetDefault,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0284C7)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("ডিফল্ট কুরআন হিসেবে সেট করুন", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            } else {
                val isPartial = (downloadStatus?.progress ?: 0) >= 10 && !mushaf.isPdf
                if (isPartial) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onSelect,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("পড়ুন (আংশিক)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onDownload,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981)),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                        ) {
                            Text("ডাউনলোড করুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFAF6EB)) // Soft cream page background color
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
    ) {
        AsyncImage(
            model = mushaf.thumbnailUrl,
            contentDescription = "Page Preview",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (isDownloaded) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
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
