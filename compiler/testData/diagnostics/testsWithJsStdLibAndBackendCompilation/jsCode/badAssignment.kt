// LANGUAGE: -IrIntraModuleInlinerBeforeKlibSerialization -IrCrossModuleInlinerBeforeKlibSerialization
// RUN_PIPELINE_TILL: BACKEND

fun Int.foo(x: Int) {
    js(<!JSCODE_ERROR!>"this = x;"<!>)
}
