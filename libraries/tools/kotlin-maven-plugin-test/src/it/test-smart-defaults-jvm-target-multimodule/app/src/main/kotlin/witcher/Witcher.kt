package witcher

/** Kotlin class that depends on Java [Potion] from lib. Direction: Kotlin -> Java */
class Witcher(val name: String, val favoriteSign: Sign) {

    fun drinkPotion(potion: Potion): String =
        "$name drinks ${potion.name} and enhances ${potion.enhancedSign.element} power"

    fun cast(): String = "$name casts ${favoriteSign.name}, a ${favoriteSign.element} sign"
}
