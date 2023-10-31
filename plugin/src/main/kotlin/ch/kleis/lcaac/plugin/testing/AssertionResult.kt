package ch.kleis.lcaac.plugin.testing

import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.basic.BasicNumber

sealed interface AssertionResult {
    fun isSuccess(): Boolean
}

data class RangeAssertionSuccess(
    val assertion: RangeAssertion,
    val actual: QuantityValue<BasicNumber>,
) : AssertionResult {
    override fun isSuccess() = true
}

data class GenericFailure(val message: String) : AssertionResult {
    override fun isSuccess() = false
}

data class RangeAssertionFailure(val assertion: RangeAssertion, val actual: QuantityValue<BasicNumber>) :
    AssertionResult {
    override fun isSuccess() = false
}
