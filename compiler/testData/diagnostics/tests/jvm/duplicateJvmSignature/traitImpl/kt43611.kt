// RUN_PIPELINE_TILL: BACKEND
// DIAGNOSTICS: -UNUSED_PARAMETER

interface A {
    <!CONFLICTING_JVM_DECLARATIONS!>fun f(a: List<Int>): String = TODO()<!>
    private <!CONFLICTING_JVM_DECLARATIONS!>fun f(a: List<String>): String = TODO()<!>
}

class B : A

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, interfaceDeclaration */
