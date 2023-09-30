package ch.kleis.lcaac.plugin.testing

import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.psi.LcaTest

data class LcaTestResult(
    val name: String,
    val results: List<AssertionResult>,
    val source: LcaTest,
)

sealed interface AssertionResult

data class RangeAssertionSuccess(
    val assertion: RangeAssertion,
    val actual: QuantityValue<BasicNumber>,
) : AssertionResult

data class GenericFailure(val message: String): AssertionResult

data class RangeAssertionFailure(val assertion: RangeAssertion, val actual: QuantityValue<BasicNumber>) : AssertionResult
