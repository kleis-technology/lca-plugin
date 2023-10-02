package ch.kleis.lcaac.plugin.testing

import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.basic.BasicNumber

sealed interface AssertionResult

data class RangeAssertionSuccess(
    val assertion: RangeAssertion,
    val actual: QuantityValue<BasicNumber>,
) : AssertionResult

data class GenericFailure(val message: String): AssertionResult

data class RangeAssertionFailure(val assertion: RangeAssertion, val actual: QuantityValue<BasicNumber>) : AssertionResult
