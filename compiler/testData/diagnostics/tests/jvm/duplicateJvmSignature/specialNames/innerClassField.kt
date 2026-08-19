// RUN_PIPELINE_TILL: BACKEND
// DIAGNOSTICS: -UNUSED_PARAMETER

class C {
    inner class D {
        val `this$0`: C? = null
    }
}

/* GENERATED_FIR_TAGS: classDeclaration, inner, nullableType, propertyDeclaration */
