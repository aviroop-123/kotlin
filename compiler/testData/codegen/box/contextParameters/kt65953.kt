// LANGUAGE: +ContextParameters

import kotlin.coroutines.*

typealias ContextBlock<T> = suspend context(Context<T>) T.(T) -> Unit

var buffer = ""

class Context<T>(private val subject : T, private val ctxName: String) {

    fun doInContext() {
        buffer = "$ctxName:$subject"
    }

    suspend fun execute(block: ContextBlock<T>) {
        block(this, subject, subject)
    }
}

data class Subject(val name : String)

suspend fun test(subject : Subject, block : ContextBlock<Subject>) = Context(subject, "my other context").execute(block)

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(Continuation(EmptyCoroutineContext) {
        it.getOrThrow()
    })
}

fun box(): String {
    val subject = Subject("John DOE")
    builder { Context(subject,"my context").execute { contextOf<Context<Subject>>().doInContext() } } // Should output "In context <my context> with subject Subject(name=John DOE)"
    if (buffer != "my context:Subject(name=John DOE)") return "FAIL 1: $buffer"
    builder { test(subject) { contextOf<Context<Subject>>().doInContext() } } // Should output "In context <my other context> with subject Subject(name=John DOE)"
    if (buffer != "my other context:Subject(name=John DOE)") return "FAIL 2: $buffer"
    return "OK"
}
