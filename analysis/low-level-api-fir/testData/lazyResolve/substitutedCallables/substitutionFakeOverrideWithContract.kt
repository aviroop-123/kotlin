// MEMBER_NAME_FILTER: property
abstract class S<caret>ubClass: AbstractClass<Int>()

abstract class AbstractClass<T> {
    val T.property: T
        get() {
            contract {
                returns(true) implies (this@AbstractClass is AbstractClass<String>)
            }

            return null!!
        }
}
