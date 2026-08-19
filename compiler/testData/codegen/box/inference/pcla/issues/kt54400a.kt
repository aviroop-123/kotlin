// ISSUE: KT-54400

fun box(): String {
    build {
        typeVariableMutableProperty = ""
    }
    return "OK"
}




class Buildee<TV> {
    var typeVariableMutableProperty: TV
        get() = storage
        set(value) { storage = value }
    private var storage: TV = "" as TV
}

fun <PTV> build(instructions: Buildee<PTV>.() -> Unit): Buildee<PTV> {
    return Buildee<PTV>().apply(instructions)
}
