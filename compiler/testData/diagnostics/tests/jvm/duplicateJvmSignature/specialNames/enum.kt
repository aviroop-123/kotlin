// RUN_PIPELINE_TILL: BACKEND
// DIAGNOSTICS: -UNUSED_PARAMETER

<!CONFLICTING_JVM_DECLARATIONS!>enum class E {
    A;

    <!CONFLICTING_JVM_DECLARATIONS!>fun values(): Array<E> = null!!<!>
    <!CONFLICTING_JVM_DECLARATIONS!>fun valueOf(s: String): E = null!!<!>
}<!>

/* GENERATED_FIR_TAGS: checkNotNullCall, enumDeclaration, enumEntry, functionDeclaration */
