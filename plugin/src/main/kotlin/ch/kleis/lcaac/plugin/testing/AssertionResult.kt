package ch.kleis.lcaac.plugin.testing

import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.math.basic.BasicNumber

data class LcaTestResult(
    val name: String,
    val results: List<AssertionResult>
)

sealed interface AssertionResult

object Success : AssertionResult {
    override fun toString(): String = "success"
}

data class GenericFailure(val message: String): AssertionResult {
    override fun toString(): String = message
}

data class RangeAssertionFailure(val assertion: RangeAssertion, val actual: QuantityValue<BasicNumber>) : AssertionResult {
    override fun toString(): String =
        "${assertion.ref} = $actual is not in between ${assertion.lo} and ${assertion.hi}"
}
