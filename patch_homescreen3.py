with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# For subTextForHero
content = content.replace(
    '                            "HAFEZI" -> "হাফেজী কুরআন (১৫ লাইন)"\n                            "TAJWEED" -> "রঙিন তাজবীদ কুরআন"',
    '                            "HAFEZI" -> "হাফেজী কুরআন (১৫ লাইন)"\n                            "TAJWEED" -> "রঙিন তাজবীদ কুরআন"'
)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
