package ch.kleis.lcaac.plugin.core.assessment

import ch.kleis.lcaac.plugin.core.lang.value.ProcessValue
import ch.kleis.lcaac.plugin.core.lang.value.SystemValue
import ch.kleis.lcaac.plugin.core.math.basic.BasicMatrix
import ch.kleis.lcaac.plugin.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.core.math.basic.BasicOperations

class ContributionAnalysisProgram(
    private val system: SystemValue<BasicNumber>,
    private val targetProcess: ProcessValue<BasicNumber>,
) {
    fun run(): ContributionAnalysis<BasicNumber, BasicMatrix> {
        val results = AnalysisProgram(system, targetProcess, BasicOperations).run()
        return ContributionAnalysis(
            results.impactFactors,
            results.intensity,
            results.allocatedSystem,
            BasicOperations,
        )
    }
}
