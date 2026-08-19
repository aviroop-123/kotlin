// LANGUAGE: +ExplicitBackingFields

val field: String = "OK"

val a: Any
    field = field

fun box(): String {
    return a
}
