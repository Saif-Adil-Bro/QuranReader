import sys

file_path = "app/src/main/java/com/example/ui/screens/PostsScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

import_statement = "import com.example.utils.DateUtil\n"
if import_statement not in content:
    content = content.replace("package com.example.ui.screens\n", "package com.example.ui.screens\n" + import_statement)

time_helper = """
fun getRelativeTimeBengali(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    val minute = 60 * 1000L
    val hour = 60 * minute
    val day = 24 * hour
    
    return when {
        diff < minute -> "এইমাত্র"
        diff < hour -> "${DateUtil.toBengaliNumerals((diff / minute).toInt())} মিনিট আগে"
        diff < day -> "${DateUtil.toBengaliNumerals((diff / hour).toInt())} ঘণ্টা আগে"
        diff < 2 * day -> "গতকাল"
        else -> "${DateUtil.toBengaliNumerals((diff / day).toInt())} দিন আগে"
    }
}
"""

if "fun getRelativeTimeBengali" not in content:
    content = content + time_helper

old_time_call = "formatPostDate(post.timestamp)"
new_time_call = "getRelativeTimeBengali(post.timestamp)"

content = content.replace(old_time_call, new_time_call)

with open(file_path, "w") as f:
    f.write(content)

