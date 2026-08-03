with open("app/src/main/java/com/example/ui/screens/PostsScreen.kt", "r") as f:
    content = f.read()

bad_part = """@Composable

fun formatPostDate(timestamp: Long): String {"""

good_part = """fun formatPostDate(timestamp: Long): String {"""

content = content.replace(bad_part, good_part)

card_bad = """}

fun BlogPostCard("""

card_good = """}

@Composable
fun BlogPostCard("""

content = content.replace(card_bad, card_good)

with open("app/src/main/java/com/example/ui/screens/PostsScreen.kt", "w") as f:
    f.write(content)

print("Fixed annotations!")
