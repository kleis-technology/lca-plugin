package ch.kleis.lcaac.plugin.testing

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorBuilder
import ch.kleis.lcaac.core.datasource.resilio_db.ResilioDbConnectorBuilder
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.register.ProcessKey
import ch.kleis.lcaac.core.lang.register.Register
import ch.kleis.lcaac.core.lang.value.QuantityValueOperations
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.plugin.ide.config.LcaacConfigExtensions
import ch.kleis.lcaac.plugin.language.loader.LcaFileCollector
import ch.kleis.lcaac.plugin.language.loader.LcaLoader
import ch.kleis.lcaac.plugin.language.loader.LcaMapper
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.psi.LcaDataExpression
import ch.kleis.lcaac.plugin.psi.LcaRangeAssertion
import ch.kleis.lcaac.plugin.psi.LcaTest
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project

class LcaTestRunner(
    private val project: Project,
) {
    private val ops = BasicOperations
    private val mapper = LcaMapper(ops)
    private val config = with(LcaacConfigExtensions()) { project.lcaacConfig() }
//    private val factory = ConnectorFactory(
//        project.basePath!!,
//        config,
//        ops,
//        symbolTable,
//        listOf(
//            CsvConnectorBuilder(),
//            ResilioDbConnectorBuilder(),
//        )
//    )
//    private val sourceOps = DefaultDataSourceOperations(
//        ,
//        ops,
//        project.basePath!!,
//    )

    // TODO: Use testing objects from core package
    fun run(test: LcaTest): LcaTestResult {
        try {
            val symbolTable = runReadAction {
                val file = test.containingFile as LcaFile
                val collector = LcaFileCollector(project)
                val parser = LcaLoader(collector.collect(file), ops)
                parser.load()
            }
            val factory = ConnectorFactory(
                project.basePath!!,
                config,
                ops,
                symbolTable,
                listOf(
                    CsvConnectorBuilder(),
                    ResilioDbConnectorBuilder(),
                )
            )
            val sourceOps = DefaultDataSourceOperations(ops, config, factory.buildConnectors())
            val testCase = runReadAction { testCase(test) }
            val updatedSymbolTable = symbolTable
                .copy(
                    processTemplates = Register(symbolTable.processTemplates)
                        .plus(mapOf(ProcessKey(testCase.body.name) to testCase))
                )
            val evaluator = Evaluator(updatedSymbolTable, ops, sourceOps)
            val trace = evaluator.trace(testCase)
            val program = ContributionAnalysisProgram(trace.getSystemValue(), trace.getEntryPoint())
            val analysis = program.run()
            val assertions = assertions(symbolTable, sourceOps, test)
            val target = trace.getEntryPoint().products.first().port()
            val results = assertions.map { assertion ->
                val ports = analysis.findAllPortsByShortName(assertion.ref)
                val impact = with(QuantityValueOperations(ops)) {
                    ports.map {
                        if (analysis.isControllable(it)) analysis.getPortContribution(target, it)
                        else analysis.supplyOf(it)
                    }.reduce { acc, quantityValue -> acc + quantityValue }
                }
                assertion.test(impact)
            }
            return LcaTestResult(
                test.testRef.name,
                results,
                test,
            )
        } catch (e: EvaluatorException) {
            return LcaTestResult(
                test.testRef.name,
                listOf(
                    GenericFailure(e.message ?: "unknown error")
                ),
                test,
            )
        }
    }

    private fun assertions(
        symbolTable: SymbolTable<BasicNumber>,
        sourceOps: DefaultDataSourceOperations<BasicNumber>,
        test: LcaTest
    ): List<RangeAssertion> {
        // mapper reads from psi, so we need runReadAction
        val data = runReadAction {
            Register(symbolTable.data)
                .plus(
                    test.variablesList.flatMap { it.assignmentList }
                        .map { DataKey(it.getDataRef().name) to mapper.dataExpression(it.getValue()) }
                )
        }
        val reducer = DataExpressionReducer(data, symbolTable.dataSources, ops, sourceOps)
        return runReadAction {
            test.assertList.flatMap { it.rangeAssertionList }
                .map {
                    val loExpression = mapper.dataExpression(it.lo())
                    val loReduced = reducer.reduce(loExpression)
                    val hiExpression = mapper.dataExpression(it.hi())
                    val hiReduced = reducer.reduce(hiExpression)
                    val lo = with(ToValue(ops)) { loReduced.toValue() }
                    val hi = with(ToValue(ops)) { hiReduced.toValue() }
                    RangeAssertion(it.uid.name, lo, hi)
                }
        }
    }

    private fun LcaRangeAssertion.lo(): LcaDataExpression {
        return this.dataExpressionList[0]
    }

    private fun LcaRangeAssertion.hi(): LcaDataExpression {
        return this.dataExpressionList[1]
    }

    private fun testCase(test: LcaTest): EProcessTemplate<BasicNumber> {
        with(mapper) {
            val name = test.testRef.name
            return EProcessTemplate(
                params = emptyMap(),
                locals = test.variablesList.flatMap { it.assignmentList }
                    .associate { it.getDataRef().name to dataExpression(it.getValue()) },
                body = EProcess(
                    name = "__test__${test.testRef.name}__",
                    products = listOf(
                        ETechnoExchange(
                            EQuantityScale(BasicNumber(1.0), EDataRef("u")),
                            EProductSpec(name, EDataRef("u"))
                        )
                    ),
                    inputs = test.givenList
                        .flatMap { it.technoInputExchangeList }
                        .map { technoInputExchange(it) },
                )
            )
        }
    }
}
