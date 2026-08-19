// CHECK_TYPESCRIPT_DECLARATIONS
// SKIP_NODE_JS
// INFER_MAIN_MODULE
// WITH_STDLIB
// LANGUAGE: +JsAllowExportingStarProjection

// MODULE: JS_TESTS
// FILE: star-projection.kt

// Position 1: sole type arg in parameter
@JsExport
fun acceptList(x: List<*>): Unit {}

// Position 2: sole type arg in return type
@JsExport
fun returnList(): List<*> = emptyList<Any>()

// Position 3: star as the first of multiple type args
@JsExport
fun acceptMapStarKey(x: Map<*, String>): Unit {}

// Position 4: star as a non-first type arg
@JsExport
fun acceptMapStarValue(x: Map<String, *>): Unit {}

// Position 5: star in all type arg positions
@JsExport
fun acceptMapStarAll(x: Map<*, *>): Unit {}

// Position 6: star nested inside another type arg
@JsExport
fun acceptNestedStar(x: List<List<*>>): Unit {}

// Position 7: star combined with nullability in parameter
@JsExport
fun acceptNullableStar(x: List<*>?): Unit {}

// Position 8: star combined with nullability in return type
@JsExport
fun returnNullableStar(): List<*>? = null

// Position 9: star nested in the value position of a multi-arg type
@JsExport
fun acceptMapWithStarList(x: Map<String, List<*>>): Unit {}

// Positions 10-13: all positions inside a class
@JsExport
class StarProjectionClass(val items: List<*>) {
    val mapping: Map<*, *> = emptyMap<Any, Any>()
    fun processEntries(x: Map<*, *>): List<*> = emptyList<Any>()
    fun getNestedMap(): Map<String, List<*>> = emptyMap()
}

@JsExport
fun box() = "OK"
