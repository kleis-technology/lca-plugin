package ch.kleis.lcaac.plugin.language.type_checker

import ch.kleis.lcaac.plugin.language.psi.stub.process.ProcessStubKeyIndex
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertFailsWith

@RunWith(JUnit4::class)
class LcaMatchLabelsEvaluatorTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return ""
    }

    @Test
    fun test_eval_whenLiteral() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    inputs {
                        1 kg carrot from carrot_production match (geo = "FR")
                    }
                }
            """.trimIndent()
        )
        val element = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first().terminalTechnoInputExchange!!
        val labels = element
            .inputProductSpec!!
            .getProcessTemplateSpec()!!
            .getMatchLabels()!!
        val sut = LcaMatchLabelsEvaluator()

        // when
        val actual = sut.eval(labels)

        // then
        val expected = mapOf("geo" to "FR")
        assertEquals(expected, actual)
    }

    @Test
    fun test_eval_whenRefToLocalVariables() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    variables {
                        country = "FR"
                    }
                    inputs {
                        1 kg carrot from carrot_production match (geo = country)
                    }
                }
            """.trimIndent()
        )
        val first = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first().terminalTechnoInputExchange!!
        val labels = first
            .inputProductSpec!!
            .getProcessTemplateSpec()!!
            .getMatchLabels()!!
        val sut = LcaMatchLabelsEvaluator()

        // when
        val actual = sut.eval(labels)

        // then
        val expected = mapOf("geo" to "FR")
        assertEquals(expected, actual)
    }

    @Test
    fun test_eval_whenRefToParams() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    params {
                        country = "FR"
                    }
                    inputs {
                        1 kg carrot from carrot_production match (geo = country)
                    }
                }
            """.trimIndent()
        )
        val first = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first().terminalTechnoInputExchange!!
        val labels = first
            .inputProductSpec!!
            .getProcessTemplateSpec()!!
            .getMatchLabels()!!
        val sut = LcaMatchLabelsEvaluator()

        // when
        val actual = sut.eval(labels)

        // then
        val expected = mapOf("geo" to "FR")
        assertEquals(expected, actual)
    }

    @Test
    fun test_eval_whenRefToLabels() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    labels {
                        country = "FR"
                    }
                    inputs {
                        1 kg carrot from carrot_production match (geo = country)
                    }
                }
            """.trimIndent()
        )
        val first = ProcessStubKeyIndex.findProcesses(
            project,
            "$pkgName.p",
            mapOf("country" to "FR")
        ).first()
            .getInputs().first().terminalTechnoInputExchange!!
        val labels = first
            .inputProductSpec!!
            .getProcessTemplateSpec()!!
            .getMatchLabels()!!
        val sut = LcaMatchLabelsEvaluator()

        // when
        val actual = sut.eval(labels)

        // then
        val expected = mapOf("geo" to "FR")
        assertEquals(expected, actual)
    }

    @Test
    fun test_eval_whenRefToGlobals() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                variables {
                    country = "FR"
                }
                
                process p {
                    inputs {
                        1 kg carrot from carrot_production match (geo = country)
                    }
                }
            """.trimIndent()
        )
        val first = ProcessStubKeyIndex.findProcesses(
            project,
            "$pkgName.p"
        ).first()
            .getInputs().first().terminalTechnoInputExchange!!
        val labels = first
            .inputProductSpec!!
            .getProcessTemplateSpec()!!
            .getMatchLabels()!!
        val sut = LcaMatchLabelsEvaluator()

        // when
        val actual = sut.eval(labels)

        // then
        val expected = mapOf("geo" to "FR")
        assertEquals(expected, actual)
    }

    @Test
    fun test_eval_whenCircularRefs() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                
                process p {
                    variables {
                        y = country
                        x = y
                        country = x
                    }
                    inputs {
                        1 kg carrot from carrot_production match (geo = country)
                    }
                }
            """.trimIndent()
        )
        val first = ProcessStubKeyIndex.findProcesses(
            project,
            "$pkgName.p"
        ).first()
            .getInputs().first().terminalTechnoInputExchange!!
        val labels = first
            .inputProductSpec!!
            .getProcessTemplateSpec()!!
            .getMatchLabels()!!
        val sut = LcaMatchLabelsEvaluator()

        // when/then
        val e = assertFailsWith<PsiTypeCheckException> { sut.eval(labels) }
        assertEquals("""circular dependencies: "country ...", "country ...", "x ...", "y ..."""", e.message)
    }

    @Test
    fun test_eval_whenRefToQuantity() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                
                process p {
                    variables {
                        country = 1 kg
                    }
                    inputs {
                        1 kg carrot from carrot_production match (geo = country)
                    }
                }
            """.trimIndent()
        )
        val first = ProcessStubKeyIndex.findProcesses(
            project,
            "$pkgName.p"
        ).first()
            .getInputs().first().terminalTechnoInputExchange!!
        val labels = first
            .inputProductSpec!!
            .getProcessTemplateSpec()!!
            .getMatchLabels()!!
        val sut = LcaMatchLabelsEvaluator()

        // when/then
        val e = assertFailsWith<PsiTypeCheckException> { sut.eval(labels) }
        assertEquals("""1 kg is not a valid label""", e.message)
    }

    @Test
    fun test_eval_whenUnresolvedRef() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    inputs {
                        1 kg carrot from carrot_production match (geo = country)
                    }
                }
            """.trimIndent()
        )
        val first = ProcessStubKeyIndex.findProcesses(
            project,
            "$pkgName.p"
        ).first()
            .getInputs().first().terminalTechnoInputExchange!!
        val labels = first
            .inputProductSpec!!
            .getProcessTemplateSpec()!!
            .getMatchLabels()!!
        val sut = LcaMatchLabelsEvaluator()

        // when/then
        val e = assertFailsWith<PsiTypeCheckException> { sut.eval(labels) }
        assertEquals("""unresolved reference country""", e.message)
    }
}
