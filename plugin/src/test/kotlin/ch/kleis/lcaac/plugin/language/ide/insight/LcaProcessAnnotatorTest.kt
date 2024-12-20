package ch.kleis.lcaac.plugin.language.ide.insight

import ch.kleis.lcaac.plugin.language.psi.stub.process.ProcessStubKeyIndex
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class LcaProcessAnnotatorTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "testdata"
    }

    @Test
    fun testAnnotate_whenCoproductIsMissingAllocationField() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    products {
                        1 kg a
                    }
                    products {
                        1 kg b
                    }
                }
            """.trimIndent()
        )
        val element = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
        val mock = AnnotationHolderMock()
        val annotator = LcaProcessAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify {
            mock.holder.newAnnotation(
                HighlightSeverity.ERROR,
                "some products in [a, b] are missing allocation factors"
            )
        }
        verify { mock.builder.range(element.blockProductsList.first()) }
        verify { mock.builder.highlightType(ProblemHighlightType.ERROR) }
        verify { mock.builder.create() }
    }

    @Test
    fun testAnnotate_whenUniqueProductWithoutAllocation_thenDoNotAnnotate() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    products {
                        1 kg a
                    }
                }
            """.trimIndent()
        )
        val element = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
        val mock = AnnotationHolderMock()
        val annotator = LcaProcessAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify(exactly = 0) { mock.holder.newAnnotation(any(), any()) }
        verify(exactly = 0) { mock.builder.create() }
    }

    @Test
    fun testAnnotate_whenMultipleProductsWithCorrectAllocationFactors_shouldDoNothing() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
                package $pkgName
                
                process p {
                    products {
                        1 kg a allocate 20 percent
                    }
                    products {
                        1 kg b allocate 80 percent
                    }
                }
            """.trimIndent()
        )
        val element = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
        val mock = AnnotationHolderMock()
        val annotator = LcaProcessAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify(exactly = 0) { mock.holder.newAnnotation(any(), any()) }
        verify(exactly = 0) { mock.builder.create() }
    }
}
