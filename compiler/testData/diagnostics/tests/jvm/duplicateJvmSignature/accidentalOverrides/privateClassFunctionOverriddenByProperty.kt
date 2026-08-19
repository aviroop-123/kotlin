// RUN_PIPELINE_TILL: BACKEND
open class B {
    private fun getX() = 1
}

class C : B() {
    val x: Int
        get() = 1
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, getter, integerLiteral, propertyDeclaration */
