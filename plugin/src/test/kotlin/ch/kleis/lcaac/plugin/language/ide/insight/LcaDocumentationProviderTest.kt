package ch.kleis.lcaac.plugin.language.ide.insight

import ch.kleis.lcaac.plugin.language.ide.syntax.LcaCompletionTestCase
import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.psi.util.PsiUtilBase
import io.mockk.every
import io.mockk.mockkObject
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

private const val mockk_message = "ok_generator"

@RunWith(JUnit4::class)
class LcaDocumentationProviderTest : LcaCompletionTestCase() {
    @Before
    fun init() {
        mockkObject(LcaDocumentGenerator)
    }

    @Test
    fun cursorOnUnitDeclaration_ShouldTriggerDoc() {
        // Given
        every { LcaDocumentGenerator.generateUnitDefinition(any()) } returns mockk_message
        // When + Then
        doTest(
            getContent(Caret.UNIT_DECLARATION)
        )
    }

    @Test
    fun cursorOnUnitAlias_ShouldTriggerDoc() {
        // Given
        every { LcaDocumentGenerator.generateUnitDefinition(any()) } returns mockk_message
        // When + Then
        doTest(
            getContent(Caret.UNIT_ALIAS)
        )
    }

    @Test
    fun cursorOnUnitInVariarableDeclaration_ShouldTriggerDoc() {
        // Given
        every { LcaDocumentGenerator.generateUnitDefinition(any()) } returns mockk_message
        // When + Then
        doTest(
            getContent(Caret.UNIT_VAR_DECLARATION)
        )
    }

    @Test
    fun cursorOnUnitInQtyReference_ShouldTriggerDoc() {
        // Given
        every { LcaDocumentGenerator.generateUnitDefinition(any()) } returns mockk_message

        // When + Then
        doTest(
            getContent(Caret.UNIT_QTY_REF)
        )
    }

    @Test
    fun cursorOnSubstanceDeclaration_ShouldTriggerDoc() {
        // Given
        every { LcaDocumentGenerator.generateSubstance(any()) } returns mockk_message

        // When + Then
        doTest(
            getContent(Caret.SUBSTANCE_DECLARATION)
        )
    }

    @Test
    fun cursorOnSubstanceReference_ShouldTriggerDoc() {
        // Given
        every { LcaDocumentGenerator.generateSubstance(any()) } returns mockk_message

        // When + Then
        doTest(
            getContent(Caret.SUBSTANCE_REF)
        )
    }


    @Test
    fun cursorOnProductDeclaration_ShouldTriggerDoc() {
        // Given
        every { LcaDocumentGenerator.generateProduct(any()) } returns mockk_message

        // When + Then
        doTest(
            getContent(Caret.PROD_DECLARATION)
        )
    }

    @Test
    fun cursorOnProductReference_ShouldTriggerDoc() {
        // Given
        every { LcaDocumentGenerator.generateProduct(any()) } returns mockk_message

        // When + Then
        doTest(
            getContent(Caret.PROD_REF)
        )
    }

    @Test
    fun cursorOnGlobalAssignmentDeclaration_ShouldTriggerDoc() {
        // Given
        every { LcaDocumentGenerator.generateGlobalAssignment(any()) } returns mockk_message

        // When + Then
        doTest(
            getContent(Caret.GLOBAL_DECLARATION)
        )
    }

    @Test
    fun cursorOnGlobalAssignmentReference_ShouldTriggerDoc() {
        // Given
        every { LcaDocumentGenerator.generateGlobalAssignment(any()) } returns mockk_message

        // When + Then
        doTest(
            getContent(Caret.GLOBAL_REF)
        )
    }

    @Test
    fun cursorOnLocalAssignmentDeclaration_ShouldTriggerDoc() {
        // Given
        every { LcaDocumentGenerator.generateAssignment(any()) } returns mockk_message

        // When + Then
        doTest(
            getContent(Caret.LOCAL_DECLARATION)
        )
    }

    @Test
    fun cursorOnLocalAssignmentReference_ShouldTriggerDoc() {
        // Given
        every { LcaDocumentGenerator.generateAssignment(any()) } returns mockk_message

        // When + Then
        doTest(
            getContent(Caret.LOCAL_REF)
        )
    }

    @Test
    fun cursorOnProcessDeclaration_ShouldTriggerDoc() {
        // Given
        every { LcaDocumentGenerator.generateProcess(any()) } returns mockk_message

        // When + Then
        doTest(
            getContent(Caret.PROCESS_DECLARATION)
        )
    }

    @Test
    fun cursorOnProcessReference_ShouldTriggerDoc_WhenReceiveAProcessRef() {
        // Given
        every { LcaDocumentGenerator.generateProcess(any()) } returns mockk_message

        // When + Then
        doTest(
            getContent(Caret.PROCESS_REF)
        )
    }

    @Test
    fun cursorOnComplexProcessReference_ShouldTriggerDoc_WhenReceiveAndLcaUID() {
        // Given
        every { LcaDocumentGenerator.generateProcess(any()) } returns mockk_message

        // When + Then
        doTest(
            getContent(Caret.PROCESS_REF2)
        )
    }

    @Test
    fun cursorOnProcessReference_ShouldNotFail_WhenReceiveANonValidProcessRef() {
        // Given

        // When + Then
        doTest(getContent(Caret.PROCESS_REF3), true)
    }

    enum class Caret {
        UNIT_DECLARATION, UNIT_ALIAS, UNIT_VAR_DECLARATION, UNIT_QTY_REF,
        SUBSTANCE_REF, SUBSTANCE_DECLARATION,
        PROD_REF, PROD_DECLARATION,
        LOCAL_REF, LOCAL_DECLARATION,
        GLOBAL_REF, GLOBAL_DECLARATION,
        PROCESS_REF, PROCESS_REF2, PROCESS_REF3, PROCESS_DECLARATION
    }

    private fun getContent(car: Caret): String {
        return """
            package totoww

            unit k${caret(car, Caret.UNIT_DECLARATION)}g2 {
                symbol = "kgbis"
                alias_for = 1.0001  k${caret(car, Caret.UNIT_ALIAS)}g
            }

            variables {
                gl${caret(car, Caret.GLOBAL_DECLARATION)}o = 1 k${caret(car, Caret.UNIT_VAR_DECLARATION)}g
            }

            process fa${caret(car, Caret.PROCESS_DECLARATION)}rm {

                params {
                    qt${caret(car, Caret.LOCAL_DECLARATION)}y = 1 g
                }

                products {
                   1 k${caret(car, Caret.UNIT_QTY_REF)}g car${caret(car, Caret.PROD_DECLARATION)}rot
                }

                inputs {
                   3 g ca${caret(car, Caret.PROD_REF)}rrot from far${caret(car, Caret.PROCESS_REF)}m
                   2 g carrot from far${caret(car, Caret.PROCESS_REF2)}m2 match (model = "bio")
                   2 g carrot from bad${caret(car, Caret.PROCESS_REF3)}bad
                   qt${caret(car, Caret.LOCAL_REF)}y salad
                   gl${caret(car, Caret.GLOBAL_REF)}o salad
                }

                emissions {
                    1.5 g myS${caret(car, Caret.SUBSTANCE_REF)}ubstance(compartment="air")
                }
            }
            process farm2 {
                labels {
                    model = "bio"
                }
                products {
                   1 kg carrot
                }

            }

            substance mySub${caret(car, Caret.SUBSTANCE_DECLARATION)}stance {
                name = "mySubstanceName"
                type = Emission
                compartment = "air"
                reference_unit = kg2
                meta {
                    "description" = "My doc"
                    "author" = "My name"
                }

                impacts {
                    48.218 u Climate_change
                }
            }
        """.trimIndent()
    }

    private fun caret(current: Caret, required: Caret) = if (current == required) "<caret>" else ""

    private fun doTest(content: String, expectedNullText: Boolean = false) {
        fixture.configureByText("sample.lca", content)
        val originalElt = PsiUtilBase.getElementAtCaret(fixture.editor)
        val element = DocumentationManager.getInstance(myFixture!!.project).findTargetElement(
            myFixture!!.editor,
            myFixture!!.file
        )
        val provider = DocumentationManager.getProviderFromElement(element)
        kotlin.test.assertNotNull(provider)
        val text = provider.generateDoc(element, originalElt)
        if (expectedNullText) {
            TestCase.assertNull(text)
        } else {
            kotlin.test.assertNotNull(text)
            assertEquals(mockk_message, text)
        }
    }


}