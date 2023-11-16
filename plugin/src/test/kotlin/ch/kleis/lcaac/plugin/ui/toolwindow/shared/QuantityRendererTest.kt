package ch.kleis.lcaac.plugin.ui.toolwindow.shared

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class QuantityRendererTest(
    private val value: Double,
    private val expected: String,
) {
    companion object {
        @Parameterized.Parameters
        @JvmStatic
        fun getDisplayedStrings(): Collection<Array<Any>> {
            return listOf(
                arrayOf(0.0, "0"),
                arrayOf(0.0001, "1E-4"),
                arrayOf(0.001, "1E-3"),
                arrayOf(0.01, "1E-2"),
                arrayOf(0.1, "1E-1"),
                arrayOf(1.0, "1"),
                arrayOf(10.0, "1E1"),
                arrayOf(100.0, "1E2"),
                arrayOf(1000.0, "1E3"),

                arrayOf(0.000999, "9.99E-4"),
                arrayOf(0.00999, "9.99E-3"),
                arrayOf(0.0999, "9.99E-2"),
                arrayOf(0.999, "9.99E-1"),
                arrayOf(9.99, "9.99"),
                arrayOf(99.9, "9.99E1"),
                arrayOf(999.0, "9.99E2"),

                arrayOf(0.00123, "1.23E-3"),
                arrayOf(0.0123, "1.23E-2"),
                arrayOf(0.123, "1.23E-1"),
                arrayOf(1.2345, "1.23"),
                arrayOf(12.345, "1.23E1"),
                arrayOf(123.45, "1.23E2"),
                arrayOf(1234.5, "1.23E3"),
                arrayOf(12345.123, "1.23E4"),

                arrayOf(-0.0, "0"),
                arrayOf(-0.00123, "-1.23E-3"),
                arrayOf(-0.0123, "-1.23E-2"),
                arrayOf(-0.123, "-1.23E-1"),
                arrayOf(-1.2345, "-1.23"),
                arrayOf(-12.345, "-1.23E1"),
                arrayOf(-123.45, "-1.23E2"),
                arrayOf(-1234.5, "-1.23E3"),
                arrayOf(-12345.123, "-1.23E4"),
            )
        }
    }

    @Test
    fun run() {
        // when
        val actual = QuantityRenderer.formatDouble(value)

        // then
        Assert.assertEquals(expected, actual)
    }
}
