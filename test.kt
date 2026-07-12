interface MyApi {
    fun doSomething(a: Int, b: String = "default"): String
}
class MyApiImpl : MyApi {
    override fun doSomething(a: Int, b: String): String {
        return "a: $a, b: $b"
    }
}
fun main() {
    val api = MyApiImpl()
    println(api.doSomething(1))
}
