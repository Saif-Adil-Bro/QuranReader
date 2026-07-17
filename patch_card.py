import re

with open("app/src/main/java/com/example/ui/screens/mushaf/components/MushafCard.kt", "r") as f:
    content = f.read()

imports = """
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
"""
if "import androidx.compose.foundation.layout.aspectRatio" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.CheckCircle\n", "import androidx.compose.material.icons.filled.CheckCircle\n" + imports)

new_mushaf_card = """@Composable
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
                    .background(Color.White)
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
"""

start_idx = content.find("@Composable\nfun MushafCard(")
if start_idx != -1:
    end_idx = content.find("@Composable\nfun MushafBookCoverPreview(")
    if end_idx != -1:
        content = content[:start_idx] + new_mushaf_card + content[end_idx:]

with open("app/src/main/java/com/example/ui/screens/mushaf/components/MushafCard.kt", "w") as f:
    f.write(content)
