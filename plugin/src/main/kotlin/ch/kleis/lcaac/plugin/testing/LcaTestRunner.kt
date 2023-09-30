package ch.kleis.lcaac.plugin.testing

import ch.kleis.lcaac.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaac.core.lang.Register
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.value.QuantityValueOperations
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
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
    project: Project,
) {
    private val collector = LcaFileCollector(project)
    private val mapper = LcaMapper(BasicOperations)

    fun run(test: LcaTest): LcaTestResult {
        try {
            val symbolTable = runReadAction {
                val file = test.containingFile as LcaFile
                val parser = LcaLoader(collector.collect(file), BasicOperations)
                parser.load()
            }
            val evaluator = Evaluator(symbolTable, BasicOperations)
            val testCase = runReadAction { testCase(test) }
            val trace = evaluator.trace(testCase)
            val program = ContributionAnalysisProgram(trace.getSystemValue(), trace.getEntryPoint())
            val analysis = program.run()
            val assertions = assertions(symbolTable, test)
            val target = trace.getEntryPoint().products.first().port()
            val results = assertions.map { assertion ->
                val ports = analysis.findAllPortsByShortName(assertion.ref)
                val impact = with(QuantityValueOperations(BasicOperations)) {
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
            )
        } catch (e : EvaluatorException) {
            return LcaTestResult(
                test.testRef.name,
                listOf(
                    GenericFailure(e.message ?: "unknown error")
                )
            )
        }
    }

    private fun assertions(symbolTable: SymbolTable<BasicNumber>, test: LcaTest): List<RangeAssertion> {
        val data = Register(symbolTable.data)
            .plus(
                test.variablesList.flatMap { it.assignmentList }
                    .map { it.getDataRef().name to mapper.dataExpression(it.getValue()) }
            )
        val reducer = DataExpressionReducer(data, BasicOperations)
        return test.assertList.flatMap { it.rangeAssertionList }
            .map {
                val loExpression = mapper.dataExpression(it.lo())
                val loReduced = reducer.reduce(loExpression)
                val hiExpression = mapper.dataExpression(it.hi())
                val hiReduced = reducer.reduce(hiExpression)
                val lo = with(ToValue(BasicOperations)) { loReduced.toValue() }
                val hi = with(ToValue(BasicOperations)) { hiReduced.toValue() }
                RangeAssertion(it.uid.name, lo, hi)
            }
    }

    private fun LcaRangeAssertion.lo(): LcaDataExpression {
        return this.dataExpressionList[0]
    }

    private fun LcaRangeAssertion.hi(): LcaDataExpression {
        return this.dataExpressionList[1]
    }

    private fun testCase(test: LcaTest): EProcessTemplateApplication<BasicNumber> {
        with(mapper) {
            val name = test.testRef.name
            return EProcessTemplateApplication(
                template = EProcessTemplate(
                    params = emptyMap(),
                    locals = emptyMap(),
                    body = EProcess(
                        name = test.testRef.name,
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
                ),
                arguments = emptyMap(),
            )
        }
    }

}
