// RUN_PIPELINE_TILL: BACKEND
open class B {
    fun getX() = 1
}

class C(<!ACCIDENTAL_OVERRIDE!>val x: Int<!>) : B()

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, integerLiteral, primaryConstructor, propertyDeclaration */
