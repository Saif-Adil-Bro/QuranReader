import sys

file_path = "app/src/main/java/com/example/ui/screens/NotificationScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

content = content.replace(".androidx.compose.ui.draw.clip(androidx.compose.foundation.shape.CircleShape)", ".clip(androidx.compose.foundation.shape.CircleShape)")

if "import androidx.compose.ui.draw.clip" not in content:
    content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier\nimport androidx.compose.ui.draw.clip")

with open(file_path, "w") as f:
    f.write(content)

