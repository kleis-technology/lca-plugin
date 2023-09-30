package ch.kleis.lcaac.plugin.testing

import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.QuantityValueOperations
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations

data class RangeAssertion(
    val ref: String,
    val lo: DataValue<BasicNumber>,
    val hi: DataValue<BasicNumber>,
) {
    fun test(quantity: QuantityValue<BasicNumber>): AssertionResult {
        with(QuantityValueOperations(BasicOperations)) {
            val actual = quantity.toDouble()
            return when {
                lo is QuantityValue<BasicNumber> && hi is QuantityValue<BasicNumber> ->
                    if (lo.toDouble() <= actual && actual <= hi.toDouble()) RangeAssertionSuccess(this@RangeAssertion, quantity)
                    else RangeAssertionFailure(this@RangeAssertion, quantity)

                else -> GenericFailure("invalid range: $lo and $hi")
            }
        }
    }
}
