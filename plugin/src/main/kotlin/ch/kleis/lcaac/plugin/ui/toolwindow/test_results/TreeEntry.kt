package ch.kleis.lcaac.plugin.ui.toolwindow.test_results

import ch.kleis.lcaac.plugin.testing.AssertionResult
import ch.kleis.lcaac.plugin.testing.LcaTestResult

sealed interface TestResultTreeEntry

data class RootEntry(
    val children: MutableList<TestResultEntry>,
) : TestResultTreeEntry

data class TestResultEntry(
    val result: LcaTestResult,
    val children: MutableList<AssertionResultEntry>,
) : TestResultTreeEntry

data class AssertionResultEntry(
    val id: Int,
    val assertion: AssertionResult,
) : TestResultTreeEntry

