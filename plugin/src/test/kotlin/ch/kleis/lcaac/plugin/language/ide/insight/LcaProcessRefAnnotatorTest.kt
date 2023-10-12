package ch.kleis.lcaac.plugin.language.ide.insight

import ch.kleis.lcaac.plugin.language.psi.stub.run.RunStubKeyIndex
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiProcessRef
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LcaProcessRefAnnotatorTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "testdata"
    }

    //    @Suppress("DialogTitleCapitalization")
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
        val element: PsiProcessRef = runElement.assessList.first().getProcessRef()
        val mock = AnnotationHolderMock()
        val annotator = LcaProcessRefAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify {
            mock.holder.newAnnotation(
                HighlightSeverity.ERROR,
                "Unable to find virtual_machine"
            )
        }
        verify { mock.builder.range(element) }
        verify { mock.builder.highlightType(ProblemHighlightType.ERROR) }
        verify { mock.builder.create() }
    }
}
