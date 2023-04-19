package task

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class StdLibTest {
    @Test
    fun sanitizeString_whenNormal_thenDoNothing() {
        // given
        val s = "abcd"

        // when
        val actual = sanitizeString(s)

        // then
        val expected = "abcd"
        assertEquals(expected, actual)
    }

    @Test
    fun sanitizeString_whenSpace_shouldReplaceWithUnderscore() {
        // given
        val s = "ab   cd"

        // when
        val actual = sanitizeString(s)

        // then
        val expected = "ab_cd"
        assertEquals(expected, actual)
    }

    @Test
    fun sanitizeString_whenSpecialSymbols_shouldRemove() {
        // given
        val s = "ab(#+cd"

        // when
        val actual = sanitizeString(s)

        // then
        val expected = "ab_cd"
        assertEquals(expected, actual)
    }

    @Test
    fun sanitizeString_whenBeginsWithNumber_shouldPrependWithUnderscore() {
        // given
        val s = "123abcd"

        // when
        val actual = sanitizeString(s)

        // then
        val expected = "_123abcd"
        assertEquals(expected, actual)
    }

}
