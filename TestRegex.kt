import kotlin.text.Regex

fun main() {
    val text1 = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ"
    val text2 = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
    val text3 = "قُلْ هُوَ اللَّهُ أَحَدٌ"
    
    val regex = Regex("[\\s\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")
    val c1 = text1.replace(regex, "")
    val c2 = text2.replace(regex, "")
    val c3 = text3.replace(regex, "")
    
    println("1: " + c1 + " isBismillah: " + (c1 == "بسماللهالرحمنالرحيم" || c1 == "بسمٱللهٱلرحمنٱلرحيم"))
    println("2: " + c2 + " isBismillah: " + (c2 == "بسماللهالرحمنالرحيم" || c2 == "بسمٱللهٱلرحمنٱلرحيم"))
    println("3: " + c3)
}
