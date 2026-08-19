// RUN_PIPELINE_TILL: FRONTEND
// DIAGNOSTICS: -OPT_IN_USAGE

@JsExport
class ClassA {
    val x: String

    @JsName("constructorA")
    constructor(y: String) {
        x = "fromString:$y"
    }

    @JsName("constructorOther")
    constructor(y: Int) {
        x = "fromInt:$y"
    }
}

@JsExport
class ClassB {
    val x: String

    @JsName("constructorB")
    constructor(y: String) {
        x = "fromString:$y"
    }

    @JsName("constructorOther")
    constructor(y: Int) {
        x = "fromInt:$y"
    }
}

@JsExport
class ClassC {
    val x: String

    @JsName("constructorC")
    <!JS_NAME_CLASH!>constructor(y: String)<!> {
        x = "fromString:$y"
    }

    @JsName("constructorC")
    <!JS_NAME_CLASH!>constructor(y: Int)<!> {
        x = "fromInt:$y"
    }
}
