import sys
import re

def update_receiver(file_path):
    with open(file_path, "r") as f:
        content = f.read()

    # Find the try-catch block for saving to Firestore
    firestore_pattern = r"try \{\s*com\.example\.utils\.PostNotificationHelper\.saveSystemNotificationToFirestore\([^)]+\)\s*\} catch \(e: Exception\) \{\s*e\.printStackTrace\(\)\s*\}"
    
    local_db_code = """try {
            val db = com.example.data.local.NotificationDatabase.getDatabase(context)
            val entity = com.example.data.local.entity.LocalNotificationEntity(
                title = duaTitle,
                content = fullContent,
                category = "নোটিফিকেশন",
                author = "কুরআনিক দুআ",
                timestamp = System.currentTimeMillis()
            )
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                db.localNotificationDao().insertNotification(entity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }"""
        
    if "ReminderReceiver" in file_path:
        local_db_code = local_db_code.replace("duaTitle", "title").replace("fullContent", "message").replace("কুরআনিক দুআ", "কুরআন প্ল্যানার")
        
    content = re.sub(firestore_pattern, local_db_code, content)
    
    if "kotlinx.coroutines.GlobalScope" not in content and "kotlinx.coroutines" not in content:
        content = content.replace("import android.content.Context", "import android.content.Context\nimport kotlinx.coroutines.launch\nimport kotlinx.coroutines.GlobalScope\nimport kotlinx.coroutines.Dispatchers")

    with open(file_path, "w") as f:
        f.write(content)

update_receiver("app/src/main/java/com/example/receiver/DailyMessageReceiver.kt")
update_receiver("app/src/main/java/com/example/receiver/ReminderReceiver.kt")
