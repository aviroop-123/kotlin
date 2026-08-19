// WITH_STDLIB
// ISSUE: KT-64066

fun box(): String {
    val map = buildMap {
        put(1, 1)
        for (v in values) {}
    }
    if (map[1] != 1) return "FAIL"
    return "OK"
}
