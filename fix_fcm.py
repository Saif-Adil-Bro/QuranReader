import sys
import re

file_path = "app/src/main/java/com/example/receiver/MyFirebaseMessagingService.kt"
with open(file_path, "r") as f:
    content = f.read()

firestore_pattern = r"try \{\s*com\.example\.utils\.PostNotificationHelper\.saveSystemNotificationToFirestore\([^)]+\)\s*\} catch \(e: Exception\) \{\s*e\.printStackTrace\(\)\s*\}"

local_db_code = """try {
            val db = com.example.data.local.NotificationDatabase.getDatabase(this)
            val entity = com.example.data.local.entity.LocalNotificationEntity(
                title = finalTitle,
                content = finalText,
                category = "নোটিফিকেশন",
                author = "পুশ নোটিফিকেশন",
                timestamp = System.currentTimeMillis()
            )
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                db.localNotificationDao().insertNotification(entity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }"""

content = re.sub(firestore_pattern, local_db_code, content)

if "kotlinx.coroutines.GlobalScope" not in content and "kotlinx.coroutines.launch" not in content:
    content = content.replace("import kotlinx.coroutines.launch", "import kotlinx.coroutines.launch\nimport kotlinx.coroutines.GlobalScope")

if "import kotlinx.coroutines.GlobalScope" not in content:
    content = content.replace("package com.example.receiver", "package com.example.receiver\nimport kotlinx.coroutines.GlobalScope\n")

with open(file_path, "w") as f:
    f.write(content)

