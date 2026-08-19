// MEMBER_NAME_FILTER: resolveMe
package second

abstract class S<caret>ubClass: AbstractClass<Int>()

abstract class AbstractClass<T> {
    fun T.resolveMe(param: T): T {
        contract {
            returns(true) implies (this@AbstractClass is AbstractClass<String>)
        }

        return null!!
    }
}
