package ch.kleis.lcaac.plugin.testing

import ch.kleis.lcaac.plugin.psi.LcaTest

data class LcaTestResult(
    val name: String,
    val results: List<AssertionResult>,
    val source: LcaTest,
)
