fun main() {
    val postTitle = "কুরআন থেকে আজকের দুআ"
    val postContent = "প্রয়োজনীয় চাহিদা মেটাতে আল্লাহর অনুগ্রহ পাওয়ার জন্য মূসা (আঃ) এর দোয়া [২৮:২৪]\n\n(রাব্বি ইন্নি লিমা- আনঝালতা ইলাইয়া মিন খাইরিন ফাকির)\n\nহে আমার রব, আপনি আমার প্রতি যে অনুগ্রহ নাযিল করবেন, আমি তার মুখাপেক্ষী। (সূরা কাসাস ২৮:২৪)"
    val itTitle = "প্রয়োজনীয় চাহিদা মেটাতে আল্লাহর অনুগ্রহ পাওয়ার জন্য মূসা (আঃ) এর দোয়া [২৮:২৪]"
    val possibleTitle = postContent.lines().firstOrNull()?.trim() ?: ""
    println("possibleTitle: '$possibleTitle'")
    println("itTitle: '$itTitle'")
    println("match 1: ${itTitle.trim() == possibleTitle}")
    println("match 2: ${postContent.contains(itTitle)}")
    println("match 3: ${postTitle.contains(itTitle)}")
}
