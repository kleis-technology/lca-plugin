package ch.kleis.lcaac.plugin.actions.csv

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorBuilder
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.plugin.ide.config.LcaacConfigExtensions
import com.intellij.openapi.project.Project
import java.lang.Double.parseDouble

class CsvProcessor(
    private val project: Project,
    private val symbolTable: SymbolTable<BasicNumber>,
    lcaacConfigLoader: () -> LcaacConfig = { with(LcaacConfigExtensions()) { project.lcaacConfig() } },
) {
    private val ops = BasicOperations
    private val config = lcaacConfigLoader()
    private val factory = ConnectorFactory(
        project.basePath ?: "",
        config,
        ops,
        symbolTable,
        listOf(
            CsvConnectorBuilder(),
        )
    )
    private val sourceOps = DefaultDataSourceOperations(ops, config, factory.buildConnectors())

    fun process(request: CsvRequest): List<CsvResult> {
        val globalParameters = parseOverrides(
            symbolTable.globalParameters.toMap().mapKeys { it.key.name },
            request,
        )
        val reqName = request.processName
        val reqLabels = request.matchLabels
        val template =
            symbolTable.getTemplate(reqName, reqLabels)
                ?: throw EvaluatorException("Could not get template for ${request.processName}")
        val processParameters = parseOverrides(template.params, request)

        val st = symbolTable.overrideGlobalParameters(globalParameters)
        val evaluator = Evaluator(st, ops, sourceOps)
        val trace = evaluator.trace(template, processParameters)
        val systemValue = trace.getSystemValue()
        val entryPoint = trace.getEntryPoint()
        val program = ContributionAnalysisProgram(systemValue, entryPoint)
        val analysis = program.run()
        return entryPoint.products
            .map { output ->
                val outputPort = output.product
                val impacts = analysis.getUnitaryImpacts(outputPort)
                CsvResult(
                    request,
                    outputPort,
                    impacts,
                )
            }
    }

    private fun parseOverrides(
        data: Map<String, DataExpression<BasicNumber>>,
        request: CsvRequest
    ): Map<String, DataExpression<BasicNumber>> = data
        .mapValues { entry ->
            when (val v = entry.value) {
                is QuantityExpression<*> -> request[entry.key]?.let {
                    val amount = parseDouble(it)
                    EQuantityScale(ops.pure(amount), EUnitOf(v))
                } ?: entry.value

                is StringExpression -> request[entry.key]?.let {
                    EStringLiteral(it)
                } ?: entry.value

                else -> throw EvaluatorException("$v is not a supported data expression")
            }
        }
}
