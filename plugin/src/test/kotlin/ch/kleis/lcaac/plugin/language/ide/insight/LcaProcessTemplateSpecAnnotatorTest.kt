package ch.kleis.lcaac.plugin.language.ide.insight

import ch.kleis.lcaac.plugin.language.psi.stub.run.RunStubKeyIndex
import ch.kleis.lcaac.plugin.language.psi.type.spec.PsiProcessTemplateSpec
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LcaProcessTemplateSpecAnnotatorTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "testdata"
    }

    @Test
    fun annotate_ShouldBeActiveWhenSimpleProcessIsInError() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                run ci {
                    assess virtual_machine
                }
            """.trimIndent()
        )
        val runElement = RunStubKeyIndex.findRun(project, "$pkgName.ci").first()
        val element: PsiProcessTemplateSpec = runElement.runnableList.first().assess!!.processTemplateSpec
        val mock = AnnotationHolderMock()
        val annotator = LcaProcessTemplateSpecAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify {
            mock.holder.newAnnotation(
                HighlightSeverity.ERROR,
                "Unable to find product virtual_machine"
            )
        }
        verify { mock.builder.range(element) }
        verify { mock.builder.highlightType(ProblemHighlightType.ERROR) }
        verify { mock.builder.create() }
    }

    // TODO Faire ce test
    @Test
    fun annotate_ShouldBeActiveWhenProcessWithParamIsInError() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                run ci {
                    assess virtual_machine( q = 5 km )
                }
                process {
                    params{
                        q = 3 kg
                    }
                }
            """.trimIndent()
        )
    }

    @Test
    fun annotate_ShouldNotBeActiveWhenProcessIsSolved() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                run ci {
                    assess virtual_machinez
                }
                
                process virtual_machinez {
                }
            """.trimIndent()
        )
        val runElement = RunStubKeyIndex.findRun(project, "$pkgName.ci").first()
        val element: PsiProcessTemplateSpec = runElement.runnableList.first().assess!!.processTemplateSpec
        val mock = AnnotationHolderMock()
        val annotator = LcaProcessTemplateSpecAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify(exactly = 0) {
            mock.holder.newAnnotation(HighlightSeverity.ERROR, any())
        }
    }
}
