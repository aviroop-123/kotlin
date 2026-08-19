package org.jetbrains

import org.junit.Test
import org.junit.Assert.*

class WitcherKotlinTest {

    @Test
    fun witcherSchoolCreatesGeralt() {
        // Kotlin test → Java (WitcherSchool) → Kotlin (Witcher)
        val geralt = WitcherSchool.createWhiteWolf()
        assertEquals("Geralt", geralt.name)
        assertEquals(Sign.AARD, geralt.favoriteSign)
    }
}
