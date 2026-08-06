// TARGET_BACKEND: JVM
// WITH_STDLIB

// MODULE: lib
// HEADER_MODE
// FILE: Anno.kt
annotation class Anno(val str: String = "defaultStr", val num: Int = 42)

// MODULE: main(lib)
// FILE: Usage.kt
@Anno
class Usage

// FILE: box.kt
fun box(): String {
    val anno = Usage::class.java.getAnnotation(Anno::class.java) ?: return "FAIL: anno is null"
    if (anno.str != "defaultStr") return "FAIL str: ${anno.str}"
    if (anno.num != 42) return "FAIL num: ${anno.num}"
    return "OK"
}
