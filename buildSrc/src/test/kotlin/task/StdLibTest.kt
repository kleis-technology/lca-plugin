package task

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource


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

    @ParameterizedTest
    @CsvSource(
        "w123a>bcd, w123a_gt_bcd, Greater than conversion",
        "w123a<bcd, w123a_lt_bcd, Lesser than conversion",
        "w123a/bcd, w123a_sl_bcd, Slash conversion",
    )
    fun sanitizeString_withSpecialChar_shouldBeReplace(source: String, expected: String, desc: String) {
        // when
        val actual = sanitizeString(source)

        // then
        assertEquals(expected, actual, desc)
    }

}
