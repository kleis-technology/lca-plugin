package task

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class EcoinventMethodIndicatorMappingKtTest {

    @Test
    fun sanitize_whenNormal_thenDoNothing() {
        // given
        val s = "abcd"

        // when
        val actual = sanitize(s)

        // then
        val expected = "abcd"
        assertEquals(expected, actual)
    }

    @Test
    fun sanitize_whenSpace_shouldReplaceWithUnderscore() {
        // given
        val s = "ab   cd"

        // when
        val actual = sanitize(s)

        // then
        val expected = "ab_cd"
        assertEquals(expected, actual)
    }

    @Test
    fun sanitize_whenSpecialSymbols_shouldRemove() {
        // given
        val s = "ab(#+cd"

        // when
        val actual = sanitize(s)

        // then
        val expected = "ab_p_cd"
        assertEquals(expected, actual)
    }

    @Test
    fun sanitize_whenBeginsWithNumber_shouldPrependWithUnderscore() {
        // given
        val s = "123abcd"

        // when
        val actual = sanitize(s)

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
    fun sanitize_withSpecialChar_shouldBeReplace(source: String, expected: String, desc: String) {
        // when
        val actual = sanitize(source)

        // then
        assertEquals(expected, actual, desc)
    }

    @Test
    fun `sanitize should behave like import sanitize`() {
        val chemistryIsFun = "(+/-) 2-(2,4-dichlorophenyl)-3-(1h-1,2,4-triazole-1-yl)propyl-1,1,2,2-tetrafluoroethylether"
        val expected = "_p_sl_2_2_4_dichlorophenyl_3_1h_1_2_4_triazole_1_yl_propyl_1_1_2_2_tetrafluoroethylether"

        // when
        val result = sanitize(chemistryIsFun)

        // then
        assertEquals(expected, result)
    }
}