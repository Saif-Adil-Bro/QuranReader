import sys

file_path = "app/src/main/java/com/example/ui/screens/NotificationScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

old_click = """                                val isDuaTarget = post.author == "কুরআনিক দুআ" || post.title.contains("দুআ") || post.content.contains("দুআ") || post.category.contains("দুআ")
                                val isPlannerTarget = post.author == "কুরআন প্ল্যানার" || post.title.contains("প্ল্যানার") || post.title.contains("লক্ষ্য") || post.content.contains("প্ল্যান")
                                
                                if (isDuaTarget && onNavigateToDua != null) {
                                    onNavigateToDua(null)
                                } else if (isPlannerTarget && onNavigateToPlanner != null) {
                                    onNavigateToPlanner()
                                } else {
                                    selectedPostForReader = post 
                                }"""

new_click = """                                val isDuaTarget = post.author == "কুরআনিক দুআ" || post.title.contains("দুআ") || post.content.contains("দুআ") || post.category.contains("দুআ")
                                val isPlannerTarget = post.author == "কুরআন প্ল্যানার" || post.title.contains("প্ল্যানার") || post.title.contains("লক্ষ্য") || post.content.contains("প্ল্যান")
                                
                                if (isDuaTarget && onNavigateToDua != null) {
                                    var duaId: Int? = null
                                    try {
                                        if (com.example.data.DuaData.richDuas.isEmpty()) {
                                            com.example.data.DuaData.initialize(context)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    val possibleTitle = post.content.lines().firstOrNull()?.trim() ?: ""
                                    val foundDua = com.example.data.DuaData.richDuas.find { 
                                        it.title.trim() == possibleTitle || post.content.contains(it.title) || post.title.contains(it.title)
                                    }
                                    if (foundDua != null) {
                                        duaId = foundDua.id
                                    }
                                    onNavigateToDua(duaId)
                                } else if (isPlannerTarget && onNavigateToPlanner != null) {
                                    onNavigateToPlanner()
                                } else {
                                    selectedPostForReader = post 
                                }"""

content = content.replace(old_click, new_click)
with open(file_path, "w") as f:
    f.write(content)
