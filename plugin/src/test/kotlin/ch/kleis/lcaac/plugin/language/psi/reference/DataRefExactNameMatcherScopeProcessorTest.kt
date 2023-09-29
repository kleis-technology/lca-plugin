package ch.kleis.lcaac.plugin.language.psi.reference

import ch.kleis.lcaac.plugin.language.loader.LcaParserDefinition
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.psi.LcaDataRef
import com.intellij.testFramework.ParsingTestCase
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DataRefExactNameMatcherScopeProcessorTest : ParsingTestCase("", "lca", LcaParserDefinition()) {
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
        val dataRef = process.getInputs().first()
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
