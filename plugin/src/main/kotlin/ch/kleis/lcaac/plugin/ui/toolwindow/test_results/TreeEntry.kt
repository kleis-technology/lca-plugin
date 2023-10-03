package ch.kleis.lcaac.plugin.ui.toolwindow.test_results

import ch.kleis.lcaac.plugin.testing.*

sealed interface TestResultTreeEntry

private const val greenTick = "\u2705"
private const val redCross = "\u274C"

data class RootEntry(
    val children: MutableList<TestResultEntry>,
) : TestResultTreeEntry {
    private val nbSuccesses = children.count { it.isSuccess }
    private val nbFailures = children.size - nbSuccesses

    override fun toString(): String {
        return "Test results. $nbSuccesses passed / $nbFailures failed."
    }
}

data class TestResultEntry(
    val result: LcaTestResult,
    val children: MutableList<AssertionResultEntry>,
) : TestResultTreeEntry {
    val isSuccess: Boolean = children.all { it.isSuccess }
    private val tick = if (isSuccess) greenTick else redCross

    override fun toString(): String {
        return "$tick ${result.name}"
    }
}

data class AssertionResultEntry(
    val id: Int,
    val result: AssertionResult,
) : TestResultTreeEntry {
    val isSuccess: Boolean = result is RangeAssertionSuccess
    private val tick = if (isSuccess) greenTick else redCross

    override fun toString(): String {
        val message = when (result) {
            is GenericFailure -> result.message
            is RangeAssertionFailure -> "${result.assertion.ref} = ${result.actual} is not in between ${result.assertion.lo} and ${result.assertion.hi}"
            is RangeAssertionSuccess -> "${result.assertion.ref} = ${result.actual} is in between ${result.assertion.lo} and ${result.assertion.hi}"
        }
        return "$tick [$id] $message"
    }
}
