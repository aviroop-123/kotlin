// RUN_PIPELINE_TILL: BACKEND
class A {
    <!DUPLICATE_CLASS_NAMES!>class B<!>
}

<!DUPLICATE_CLASS_NAMES!>class `A$B`<!>

/* GENERATED_FIR_TAGS: classDeclaration, nestedClass */
