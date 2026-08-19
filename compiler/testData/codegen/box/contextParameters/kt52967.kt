// LANGUAGE: +ContextParameters

typealias HtmlFragment1 = context(PageBuilder, Array<*>) () -> Unit

var buffer = ""

internal object SnarkHtmlParser {

    fun parseText(): HtmlFragment1 = {
        "".div1 {
            buffer = "OK"
        }
    }
}

object PageBuilder

inline fun String.div1(classes : String? = null, crossinline block : () -> Unit = {}) {
    block()
}

fun box(): String {
    SnarkHtmlParser.parseText().invoke(PageBuilder, emptyArray<String>())
    return buffer
}
