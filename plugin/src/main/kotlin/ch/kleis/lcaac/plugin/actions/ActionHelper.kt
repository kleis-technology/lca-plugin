package ch.kleis.lcaac.plugin.actions

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.math.QuantityOperations
import ch.kleis.lcaac.plugin.language.loader.LcaFileCollector
import ch.kleis.lcaac.plugin.language.loader.LcaLoader
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProgressIndicator

fun <Q> traceSystemWithIndicator(
    indicator: ProgressIndicator,
    file: LcaFile,
    processName: String,
    matchLabels: Map<String, String>,
    ops: QuantityOperations<Q>,
    sourceOps: DataSourceOperations<Q>,
): EvaluationTrace<Q> {
    indicator.isIndeterminate = true

    // read
    indicator.text = "Loading symbol table"
    val symbolTable = runReadAction {
        val collector = LcaFileCollector(file.project)
        val parser = LcaLoader(collector.collect(file), ops)
        parser.load()
    }

    // compute
    indicator.text = "Solving system"
    val template = symbolTable.getTemplate(processName, matchLabels)!! // We are called from a process, so it must exist
    return Evaluator(symbolTable, ops, sourceOps).trace(template)
}
