import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

# 1. Add menu item
old_menu_items = 'MenuItem("theme", "অ্যাপ থিম", Icons.Default.Palette, Color(0xFF9C27B0)),'
new_menu_items = 'MenuItem("notifications", "নোটিফিকেশন", Icons.Default.Notifications, Color(0xFFFBBF24)),\n        MenuItem("theme", "অ্যাপ থিম", Icons.Default.Palette, Color(0xFF9C27B0)),'
content = content.replace(old_menu_items, new_menu_items)

# 2. Add to title
old_title_cases = '"theme" -> "অ্যাপ থিম"'
new_title_cases = '"notifications" -> "নোটিফিকেশন সেটিংস"\n                    "theme" -> "অ্যাপ থিম"'
content = content.replace(old_title_cases, new_title_cases)

# 3. Add to content switch
old_content_cases = '"theme" -> ThemeDialogContent(viewModel)'
new_content_cases = '"notifications" -> NotificationDialogContent(viewModel)\n                        "theme" -> ThemeDialogContent(viewModel)'
content = content.replace(old_content_cases, new_content_cases)

# 4. Append NotificationDialogContent
notification_dialog_code = """

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDialogContent(viewModel: SettingsViewModel) {
    val dailyEnabled by viewModel.dailyMessageEnabled.collectAsState()
    val dailyHour by viewModel.dailyMessageHour.collectAsState()
    val dailyMinute by viewModel.dailyMessageMinute.collectAsState()

    val context = LocalContext.current
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleDailyMessage(true)
        } else {
            android.widget.Toast.makeText(context, "নোটিফিকেশন পারমিশন প্রয়োজন", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        val timeState = androidx.compose.material3.rememberTimePickerState(
            initialHour = dailyHour,
            initialMinute = dailyMinute,
            is24Hour = false
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateDailyMessageTime(timeState.hour, timeState.minute)
                    showTimePicker = false
                }) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("বাতিল")
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.material3.TimePicker(state = timeState)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFBBF24).copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFFFBBF24))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "দৈনিক ইসলামিক বার্তা",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "প্রতিদিন আয়াত বা হাদিস রিমাইন্ডার পান",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = dailyEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                        viewModel.toggleDailyMessage(true)
                                    } else {
                                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    viewModel.toggleDailyMessage(true)
                                }
                            } else {
                                viewModel.toggleDailyMessage(false)
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryGreen, checkedTrackColor = PrimaryGreen.copy(alpha = 0.5f))
                    )
                }

                if (dailyEnabled) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Border)
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = PrimaryGreen)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("রিমাইন্ডারের সময়", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            val timeStr = String.format("%02d:%02d %s", if (dailyHour % 12 == 0) 12 else dailyHour % 12, dailyMinute, if (dailyHour >= 12) "PM" else "AM")
                            Text(timeStr, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                        }
                    }
                }
            }
        }
    }
}
"""
content += notification_dialog_code

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
