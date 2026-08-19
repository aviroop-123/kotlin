// LANGUAGE: +ContextParameters

package org.example

import kotlin.coroutines.*

data class Foo(val x: Int)
data class Bar(val x: Int)

var buffer = ""

context(_: Foo)
suspend inline fun Bar.printx() {
  buffer = "x is $x"
}


fun box(): String {
    val foo = Foo(123)

    suspend {
        with(foo) {
            Bar(456).printx()
        }
    }.startCoroutine(Continuation(EmptyCoroutineContext) {
        it.getOrThrow()
    })
    if (buffer != "x is 456") return "FAIL: $buffer"
    return "OK"
}
