// RUN_PIPELINE_TILL: BACKEND
interface B {
    fun getX() = 1
}

interface D {
    val x: Int
}

<!ACCIDENTAL_OVERRIDE!>class C(d: D) : D by d, B<!>

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, inheritanceDelegation, integerLiteral,
interfaceDeclaration, primaryConstructor, propertyDeclaration */
