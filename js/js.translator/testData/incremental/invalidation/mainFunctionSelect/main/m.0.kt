import value.ok

fun box(stepId: Int, isWasm: Boolean): String {
    val correct = when (stepId) {
        0 -> "B"
        1 -> "A"
        2 -> "A"
        3 -> "B"
        4 -> "C"
        else -> "Unknown"
    }
    callMainFromSecond() // this made to not DCE main from module <second>
    return if (correct == value.ok) "OK" else "Expected $correct but have ${value.ok}"
}
