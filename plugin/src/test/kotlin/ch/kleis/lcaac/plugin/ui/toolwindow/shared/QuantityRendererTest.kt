package ch.kleis.lcaac.plugin.ui.toolwindow.shared

import io.kotest.core.names.DuplicateTestNameMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.kotest.property.assume
import io.kotest.property.checkAll
import kotlin.math.abs

class QuantityRendererTest : DescribeSpec({
    duplicateTestNameMode = DuplicateTestNameMode.Silent
    describe("QuantityRenderer") {
        // Properties
        it("should use scientific notation with 3 significant digits for values not in [-10;-1[U]1;10]") {
            val notationRegex = Regex("-?([0-9](\\.[0-9]{1,2})?(E-?[1-9][0-9]*)?|∞)")
            checkAll<Double> { value ->
                assume(abs(value).let { it >= 10 || it > 1})
                QuantityRenderer.formatDouble(value) shouldMatch notationRegex
            }
        }
        it("should not display an exponent part for values in [-10;-1[U]1;10]") {
            val notationRegex = Regex("-?[0-9](\\.[0-9]{1,2})?")
            checkAll<Double> { value ->
                assume(abs(value).let { it < 10 && it >= 1})
                QuantityRenderer.formatDouble(value) shouldMatch notationRegex
            }
        }

        // Non-regression of corner cases found by fuzzing
        it("should properly encode infinities") {
            QuantityRenderer.formatDouble(Double.POSITIVE_INFINITY) shouldBe "∞"
            QuantityRenderer.formatDouble(Double.NEGATIVE_INFINITY) shouldBe "-∞"
        }

        it("should properly deal with -0.0") {
            QuantityRenderer.formatDouble(-0.0) shouldBe "0"
        }
    }
})