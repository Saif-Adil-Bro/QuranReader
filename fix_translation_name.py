import re

with open("app/src/main/java/com/example/ui/screens/SurahDetailScreen.kt", "r") as f:
    content = f.read()

# Replace the block
pattern = r"val fallbackNames = mapOf.*?val translatorName = translationInfo\?\.translatedName\?\.name \?\: translationInfo\?\.name \?\: fallbackNames\[trans\.resourceId\]\s*\?\: translationInfo\?\.name\s*\?\: \"Unknown\""
replacement = """val fallbackNames = mapOf(161 to "Taisirul Quran", 162 to "Bayan Foundation", 163 to "Dr. Abu Bakr Muhammad Zakaria", 213 to "Suhel Syed Siraj", 85 to "M.A.S. Abdel Haleem", 131 to "Dr. Mustafa Khattab", 20 to "Saheeh International", 97 to "Mufti Taqi Usmani", 54 to "Maulana Ashraf Ali Thanvi", 136 to "Mufti Taqi Usmani")
                            val translatorName = translationInfo?.translatedName?.name ?: translationInfo?.name ?: fallbackNames[trans.resourceId] ?: "Unknown" """

content = re.sub(pattern, replacement, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/SurahDetailScreen.kt", "w") as f:
    f.write(content)
print("Replaced")
