import sys

file_path = "app/src/main/java/com/example/ui/screens/NotificationScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

bad_chunk = """                    onNavigateToPlanner()
                                } else {
                                    selectedPostForReader = post 
                                }
                            }
                        )
                    }
                }
            }
        }
    }"""

content = content.replace(bad_chunk, "")
with open(file_path, "w") as f:
    f.write(content)

print("Fixed!")
