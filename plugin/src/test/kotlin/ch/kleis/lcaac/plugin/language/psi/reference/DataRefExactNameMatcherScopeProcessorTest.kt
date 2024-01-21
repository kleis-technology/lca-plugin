package ch.kleis.lcaac.plugin.language.psi.reference

import ch.kleis.lcaac.plugin.language.loader.LcaParserDefinition
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.psi.LcaDataRef
import ch.kleis.lcaac.plugin.psi.LcaTerminalTechnoInputExchange
import com.intellij.testFramework.ParsingTestCase
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DataRefExactNameMatcherScopeProcessorTest : ParsingTestCase("", "lca", LcaParserDefinition()) {
    @Test
    fun test_inTest_localVariable_thenCorrect() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val file = parseFile(
            "resolver", """
                package $pkgName
                
                test t {
                    variables {
                        x = 1 kg
                    }
                    assert {
                        GWP between x and 2 kg
                    }
                }
            """.trimIndent()
        ) as LcaFile
        val dataRef = file.getTests().first()
            .assertList.first()
            .rangeAssertionList.first()
            .dataExpressionList[0] as LcaDataRef

        // when
        val actual = dataRef.reference.resolve()

        // then
        val expected = file.getTests().first()
            .variablesList.first()
            .assignmentList.first()
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_whenLabel_thenCorrect() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val file = parseFile(
            "resolver", """
                package $pkgName
                
                process a {
                    labels {
                        geo = "GLO"
                    }
                    inputs {
                        1 kg carrot from carrot_production match (geo = geo)
                    }
                }
            """.trimIndent()
        ) as LcaFile
        val process = file.getProcesses().first()
        val assignment = process.getLabelsList().first().labelAssignmentList.first()
        val element = process.getInputs().first() as LcaTerminalTechnoInputExchange
        val dataRef = element
            .inputProductSpec
            .getProcessTemplateSpec()!!
            .getMatchLabels()!!
            .labelSelectorList.first()
            .dataExpression as LcaDataRef

        // when
        val actual = dataRef.reference.resolve()

        // then
        TestCase.assertEquals(assignment, actual)
    }

    @Test
    fun test_whenLocalParam_thenCorrect() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val file = parseFile(
            "resolver", """
                package $pkgName
                
                process a {
                    params {
                        x = 1 kg
                    }
                    products {
                        x carrot
                    }
                }
            """.trimIndent()
        ) as LcaFile
        val process = file.getProcesses().first()
        val assignment = process.getParamsList().first().assignmentList.first()
        val dataRef = process.getProducts().first()
            .dataExpression as LcaDataRef

        // when
        val actual = dataRef.reference.resolve()

        // then
        TestCase.assertEquals(assignment, actual)
    }

    @Test
    fun test_whenLocalVariable_thenCorrect() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        val file = parseFile(
            "resolver", """
                package $pkgName
                
                process a {
                    variables {
                        x = 1 kg
                    }
                    products {
                        x carrot
                    }
                }
            """.trimIndent()
        ) as LcaFile
        val process = file.getProcesses().first()
        val assignment = process.getVariablesList().first().assignmentList.first()
        val dataRef = process.getProducts().first()
            .dataExpression as LcaDataRef

        // when
        val actual = dataRef.reference.resolve()

        // then
        TestCase.assertEquals(assignment, actual)
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
