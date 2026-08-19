import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

val outcome: Outcome<String, Long> = null!!

fun fo<caret>o() {
    if (outcome.isIncomplete()) {

    }
}

class Outcome<out I, out T : Any> private constructor(private val value: Any) {
    fun isIncomplete(): Boolean {
        contract {
            returns(false) implies (this@Outcome is Outcome<Nothing, T>)
            returns(true) implies (this@Outcome is Outcome<I, Nothing>)
        }
        return value is Incomplete
    }

    private data class Incomplete(val incompleteValue: Any?)
}
