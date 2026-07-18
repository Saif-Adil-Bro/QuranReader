package com.example.ui.screens.mushaf.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.material.icons.filled.Check

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

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
    var showMenu by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val canRead = downloadStatus?.state == DownloadState.Downloaded || 
                              ((downloadStatus?.progress ?: 0) >= 10 && !mushaf.isPdf)
                if (canRead) {
                    onSelect()
                } else if (downloadStatus?.state != DownloadState.Downloading) {
                    onDownload()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .padding(top = 12.dp) // Space for the badge
        ) {
            // Main Cover
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .border(2.dp, Color(0xFFE5E7EB)) // Light gray border
                    .shadow(4.dp)
            ) {
                AsyncImage(
                    model = mushaf.thumbnailUrl,
                    contentDescription = "Page Preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().padding(1.dp)
                )
                
                if (downloadStatus?.state == DownloadState.Downloading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                progress = { (downloadStatus.progress / 100f) },
                                color = Color(0xFF10B981),
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "${downloadStatus.progress}%",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            // Checkmark Badge
            if (downloadStatus?.state == DownloadState.Downloaded) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-12).dp)
                        .size(24.dp)
                        .background(Color(0xFF326553), CircleShape), // Dark greenish color like screenshot
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Downloaded",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = mushaf.nameBengali,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF374151) // Dark gray
                )
                Text(
                    text = mushaf.descriptionBengali,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (downloadStatus?.state != DownloadState.Downloading && downloadStatus?.state != DownloadState.Downloaded) {
                        DropdownMenuItem(
                            text = { Text("ডাউনলোড করুন") },
                            onClick = { 
                                showMenu = false
                                onDownload()
                            }
                        )
                    }
                    if (downloadStatus?.state == DownloadState.Downloading) {
                        DropdownMenuItem(
                            text = { Text("পজ করুন") },
                            onClick = { 
                                showMenu = false
                                onPause()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("বাতিল করুন") },
                            onClick = { 
                                showMenu = false
                                onCancel()
                            }
                        )
                    }
                    if (downloadStatus?.state == DownloadState.Downloaded || ((downloadStatus?.progress ?: 0) >= 10 && !mushaf.isPdf)) {
                        DropdownMenuItem(
                            text = { Text("পড়ুন") },
                            onClick = { 
                                showMenu = false
                                onSelect()
                            }
                        )
                    }
                    if (!isDefault && downloadStatus?.state == DownloadState.Downloaded) {
                        DropdownMenuItem(
                            text = { Text("ডিফল্ট সেট করুন") },
                            onClick = { 
                                showMenu = false
                                onSetDefault()
                            }
                        )
                    }
                    if ((downloadStatus?.downloadedPages ?: 0) > 0) {
                        DropdownMenuItem(
                            text = { Text("মুছে ফেলুন", color = Color.Red) },
                            onClick = { 
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
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
