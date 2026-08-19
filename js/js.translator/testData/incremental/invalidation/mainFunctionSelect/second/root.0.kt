fun main() {
    value.ok += "!A!"
}

fun callMainFromSecond() {
    if (2.toString() == 3.toString()) {
        main()
    }
}
