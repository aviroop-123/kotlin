// RUN_PIPELINE_TILL: FRONTEND
// MODULE: library
// JVM_DEFAULT_MODE: no-compatibility
// FILE: a.kt
package base

interface UExpression {
    fun evaluate(): Any? = "fail"
}

// MODULE: main(library)
// JVM_DEFAULT_MODE: disable
// LANGUAGE: -AllowSuperCallToJavaInterface
// FILE: source.kt
import base.*

interface KotlinInterface : UExpression {
    override fun evaluate(): Any? {
        return super.<!INTERFACE_CANT_CALL_DEFAULT_METHOD_VIA_SUPER!>evaluate<!>()
    }
}

class KotlinClass : UExpression {
    override fun evaluate(): Any? {
        return super.evaluate()
    }
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, interfaceDeclaration, nullableType, override,
stringLiteral, superExpression */
