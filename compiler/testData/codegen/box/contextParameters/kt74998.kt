// LANGUAGE: +ContextParameters

fun box(): String {
    var res = ""
    with(1) {
        with("bar") {
            foo {
                res = "OK"
            }
        }
    }
    return res
}

context(_: Int)
inline fun String.foo(block: context(Int) String.() -> Unit) {
    block(1, this)
}
