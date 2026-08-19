// ISSUE: KT-65300

fun box(): String {
    build {
        class TypeInfoSourceClass: Buildee<TargetType> by this@build
    }
    return "OK"
}




class TargetType

interface Buildee<TV>

fun <PTV> build(instructions: Buildee<PTV>.() -> Unit): Buildee<PTV> {
    return (object: Buildee<PTV> {}).apply(instructions)
}
