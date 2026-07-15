fun main() {
    val raw = "بِسْمِ <tajweed class=ham_wasl>ٱ</tajweed>للَّهِ <tajweed class=ham_wasl>ٱ</tajweed><tajweed class=laam_shamsiyah>ل</tajweed>رَّحْمَ<tajweed class=madda_normal>ـٰ</tajweed>نِ <tajweed class=ham_wasl>ٱ</tajweed><tajweed class=laam_shamsiyah>ل</tajweed>رَّح<tajweed class=madda_permissible>ِي</tajweed>مِ <span class=end>١</span>"
    val regex = "<(tajweed|span)\\s+class=([a-zA-Z0-9_]+)>([^<]+)</(?:tajweed|span)>".toRegex()
    var lastIndex = 0
    var out = ""
    for (match in regex.findAll(raw)) {
        val start = match.range.first
        if (start > lastIndex) {
            out += raw.substring(lastIndex, start)
        }
        val tagType = match.groupValues[1]
        val className = match.groupValues[2]
        val content = match.groupValues[3]
        if (tagType == "span" && className == "end") {
            out += "﴿${content}﴾"
        } else if (tagType == "tajweed") {
            out += content
        } else {
            out += content
        }
        lastIndex = match.range.last + 1
    }
    if (lastIndex < raw.length) {
        out += raw.substring(lastIndex)
    }
    println(out)
}
