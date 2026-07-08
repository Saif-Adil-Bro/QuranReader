import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Typeface
import android.graphics.Typeface as AndroidTypeface
import java.io.File

fun test() {
    val file = File("test.ttf")
    val t = AndroidTypeface.createFromFile(file)
    val f = FontFamily(Typeface(t))
}
