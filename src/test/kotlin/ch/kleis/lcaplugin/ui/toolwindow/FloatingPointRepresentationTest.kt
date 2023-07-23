package ch.kleis.lcaplugin.ui.toolwindow

import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.junit.Test
import kotlin.test.assertEquals

private fun digits(s: String): List<Int> {
    return s.map { Integer.parseInt(it.toString()) }
}

@RunWith(Parameterized::class)
class FloatingPointRepresentationTest(
    private val value: Double,
    private val expected: FloatingPointRepresentation,
) {
    companion object {

        @Parameters
        @JvmStatic
        fun getFloatingPointRepresentationSamples(): Collection<Array<Any>> {
            return listOf(
                arrayOf(0.0012345, FloatingPointRepresentation(true, digits("123"), -3, 3)),
                arrayOf(0.012345, FloatingPointRepresentation(true, digits("123"), -2, 3)),
                arrayOf(0.12345, FloatingPointRepresentation(true, digits("123"), -1, 3)),
                arrayOf(1.2345, FloatingPointRepresentation(true, digits("123"), 0, 3)),
                arrayOf(12.345, FloatingPointRepresentation(true, digits("123"), 1, 3)),
                arrayOf(123.45, FloatingPointRepresentation(true, digits("123"), 2, 3)),
                arrayOf(1234.5, FloatingPointRepresentation(true, digits("123"), 3, 3)),
                arrayOf(12345.0, FloatingPointRepresentation(true, digits("123"), 4, 3)),

                arrayOf(0.0, FloatingPointRepresentation(true, digits("000"), 0, 3)),
                arrayOf(0.0001, FloatingPointRepresentation(true, digits("100"), -4, 3)),
                arrayOf(0.001, FloatingPointRepresentation(true, digits("100"), -3, 3)),
                arrayOf(0.01, FloatingPointRepresentation(true, digits("100"), -2, 3)),
                arrayOf(0.1, FloatingPointRepresentation(true, digits("100"), -1, 3)),
                arrayOf(1.0, FloatingPointRepresentation(true, digits("100"), 0, 3)),
                arrayOf(10.0, FloatingPointRepresentation(true, digits("100"), 1, 3)),
                arrayOf(100.0, FloatingPointRepresentation(true, digits("100"), 2, 3)),
                arrayOf(1000.0, FloatingPointRepresentation(true, digits("100"), 3, 3)),

                arrayOf(0.000999, FloatingPointRepresentation(true, digits("100"), -3, 3)),
                arrayOf(0.00999, FloatingPointRepresentation(true, digits("100"), -2, 3)),
                arrayOf(0.0999, FloatingPointRepresentation(true, digits("100"), -1, 3)),
                arrayOf(0.999, FloatingPointRepresentation(true, digits("100"), 0, 3)),
                arrayOf(9.99, FloatingPointRepresentation(true, digits("100"), 1, 3)),
                arrayOf(99.9, FloatingPointRepresentation(true, digits("100"), 2, 3)),
                arrayOf(999.0, FloatingPointRepresentation(true, digits("100"), 3, 3)),

                arrayOf(0.001001, FloatingPointRepresentation(true, digits("100"), -3, 3)),
                arrayOf(0.01001, FloatingPointRepresentation(true, digits("100"), -2, 3)),
                arrayOf(0.1001, FloatingPointRepresentation(true, digits("100"), -1, 3)),
                arrayOf(1.001, FloatingPointRepresentation(true, digits("100"), 0, 3)),
                arrayOf(10.01, FloatingPointRepresentation(true, digits("100"), 1, 3)),
                arrayOf(100.1, FloatingPointRepresentation(true, digits("100"), 2, 3)),
                arrayOf(1001.0, FloatingPointRepresentation(true, digits("100"), 3, 3)),

                arrayOf(0.0002999, FloatingPointRepresentation(true, digits("300"), -4, 3)),
                arrayOf(0.003999, FloatingPointRepresentation(true, digits("400"), -3, 3)),
                arrayOf(0.04999, FloatingPointRepresentation(true, digits("500"), -2, 3)),
                arrayOf(0.5999, FloatingPointRepresentation(true, digits("600"), -1, 3)),
                arrayOf(6.999, FloatingPointRepresentation(true, digits("700"), 0, 3)),
                arrayOf(79.99, FloatingPointRepresentation(true, digits("800"), 1, 3)),
                arrayOf(899.9, FloatingPointRepresentation(true, digits("900"), 2, 3)),

                arrayOf(-0.0012345, FloatingPointRepresentation(false, digits("123"), -3, 3)),
                arrayOf(-0.012345, FloatingPointRepresentation(false, digits("123"), -2, 3)),
                arrayOf(-0.12345, FloatingPointRepresentation(false, digits("123"), -1, 3)),
                arrayOf(-1.2345, FloatingPointRepresentation(false, digits("123"), 0, 3)),
                arrayOf(-12.345, FloatingPointRepresentation(false, digits("123"), 1, 3)),
                arrayOf(-123.45, FloatingPointRepresentation(false, digits("123"), 2, 3)),
                arrayOf(-1234.5, FloatingPointRepresentation(false, digits("123"), 3, 3)),
                arrayOf(-12345.0, FloatingPointRepresentation(false, digits("123"), 4, 3)),

                arrayOf(-0.0, FloatingPointRepresentation(true, digits("000"), 0, 3)),
                arrayOf(-0.0001, FloatingPointRepresentation(false, digits("100"), -4, 3)),
                arrayOf(-0.001, FloatingPointRepresentation(false, digits("100"), -3, 3)),
                arrayOf(-0.01, FloatingPointRepresentation(false, digits("100"), -2, 3)),
                arrayOf(-0.1, FloatingPointRepresentation(false, digits("100"), -1, 3)),
                arrayOf(-1.0, FloatingPointRepresentation(false, digits("100"), 0, 3)),
                arrayOf(-10.0, FloatingPointRepresentation(false, digits("100"), 1, 3)),
                arrayOf(-100.0, FloatingPointRepresentation(false, digits("100"), 2, 3)),
                arrayOf(-1000.0, FloatingPointRepresentation(false, digits("100"), 3, 3)),

                arrayOf(-0.000999, FloatingPointRepresentation(false, digits("100"), -3, 3)),
                arrayOf(-0.00999, FloatingPointRepresentation(false, digits("100"), -2, 3)),
                arrayOf(-0.0999, FloatingPointRepresentation(false, digits("100"), -1, 3)),
                arrayOf(-0.999, FloatingPointRepresentation(false, digits("100"), 0, 3)),
                arrayOf(-9.99, FloatingPointRepresentation(false, digits("100"), 1, 3)),
                arrayOf(-99.9, FloatingPointRepresentation(false, digits("100"), 2, 3)),
                arrayOf(-999.0, FloatingPointRepresentation(false, digits("100"), 3, 3)),

                arrayOf(-0.001001, FloatingPointRepresentation(false, digits("100"), -3, 3)),
                arrayOf(-0.01001, FloatingPointRepresentation(false, digits("100"), -2, 3)),
                arrayOf(-0.1001, FloatingPointRepresentation(false, digits("100"), -1, 3)),
                arrayOf(-1.001, FloatingPointRepresentation(false, digits("100"), 0, 3)),
                arrayOf(-10.01, FloatingPointRepresentation(false, digits("100"), 1, 3)),
                arrayOf(-100.1, FloatingPointRepresentation(false, digits("100"), 2, 3)),
                arrayOf(-1001.0, FloatingPointRepresentation(false, digits("100"), 3, 3)),

                arrayOf(-0.0002999, FloatingPointRepresentation(false, digits("300"), -4, 3)),
                arrayOf(-0.003999, FloatingPointRepresentation(false, digits("400"), -3, 3)),
                arrayOf(-0.04999, FloatingPointRepresentation(false, digits("500"), -2, 3)),
                arrayOf(-0.5999, FloatingPointRepresentation(false, digits("600"), -1, 3)),
                arrayOf(-6.999, FloatingPointRepresentation(false, digits("700"), 0, 3)),
                arrayOf(-79.99, FloatingPointRepresentation(false, digits("800"), 1, 3)),
                arrayOf(-899.9, FloatingPointRepresentation(false, digits("900"), 2, 3)),
            )
        }

    }

    @Test
    fun run() {
        // when
        val actual = FloatingPointRepresentation.of(value, 3)

        // then
        assertEquals(expected, actual, "value = $value")
    }

}
