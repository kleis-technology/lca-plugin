package ch.kleis.lcaac.plugin.actions

import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorBuilder
import ch.kleis.lcaac.core.datasource.resilio_db.ResilioDbConnectorBuilder
import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.math.QuantityOperations
import ch.kleis.lcaac.plugin.ide.config.LcaacConfigExtensions
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
    val config = with(LcaacConfigExtensions()) { file.project.lcaacConfig() }
    val factory = ConnectorFactory(
        file.project.basePath!!,
        config,
        ops,
        symbolTable,
        listOf(
            CsvConnectorBuilder(),
            ResilioDbConnectorBuilder(),
        )
    )
    val sourceOps = DefaultDataSourceOperations(ops, config, factory.buildConnectors())

    return Evaluator(symbolTable, ops, sourceOps).trace(template)
}
