package ch.kleis.lcaplugin.core.lang.dimension

import kotlin.test.assertEquals
import org.junit.Test


class DimensionTest {

    @Test
    fun test_toString_whenNone() {
        // given
        val dimension = Dimension.None

        // when
        val actual = "$dimension"

        // then
        assertEquals("dimensionless", actual)
    }

    @Test
    fun test_toString_whenSimpleDim() {
        // given
        val dimension = Dimension.of("something")

        // when
        val actual ="$dimension"

        // then
        assertEquals("something", actual)
    }

    @Test
    fun test_toString_whenSmallPower() {
        // given
        val dimension = Dimension.of("something", 2)

        // when
        val actual ="$dimension"

        // then
        assertEquals("something²", actual)
    }
}