package ch.kleis.lcaac.plugin.language.ide.insight

import ch.kleis.lcaac.plugin.language.psi.stub.global_assignment.GlobalAssigmentStubKeyIndex
import ch.kleis.lcaac.plugin.language.psi.stub.process.ProcessStubKeyIndex
import ch.kleis.lcaac.plugin.language.psi.stub.unit.UnitStubKeyIndex
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LcaAssignmentAnnotatorTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "testdata"
    }

    @Test
    fun testAnnotateInGlobals_whenUnique_shouldDoNothing() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            variables {
                x = 1l
            }
        """.trimIndent()
        )
        val element = GlobalAssigmentStubKeyIndex.findGlobalAssignments(project, "$pkgName.x").first()
            .getDataRef()
        val mock = AnnotationHolderMock()
        val annotator = LcaAssignmentAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify(exactly = 0) { mock.holder.newAnnotation(any(), any()) }
        verify(exactly = 0) { mock.builder.create() }
    }

    @Test
    fun testAnnotateInGlobals_whenAlsoInPrelude_shouldAnnotate() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            variables {
                kg = 1l
            }
        """.trimIndent()
        )
        val element = GlobalAssigmentStubKeyIndex.findGlobalAssignments(project, "$pkgName.kg").first()
            .getDataRef()
        val mock = AnnotationHolderMock()
        val annotator = LcaAssignmentAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify {
            mock.holder.newAnnotation(
                HighlightSeverity.ERROR,
                "This name is already defined."
            )
        }
        verify { mock.builder.range(element) }
        verify { mock.builder.highlightType(ProblemHighlightType.ERROR) }
        verify { mock.builder.create() }
    }

    @Test
    fun testAnnotateInGlobals_whenDefinedTwice_shouldAnnotate() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            variables {
                x = 1l
                x = 2l
            }
        """.trimIndent()
        )
        val element = GlobalAssigmentStubKeyIndex.findGlobalAssignments(project, "$pkgName.x").first()
            .getDataRef()
        val mock = AnnotationHolderMock()
        val annotator = LcaAssignmentAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify { mock.holder.newAnnotation(HighlightSeverity.ERROR, "This name is already defined.") }
        verify { mock.builder.range(element) }
        verify { mock.builder.highlightType(ProblemHighlightType.ERROR) }
        verify { mock.builder.create() }
    }

    @Test
    fun testAnnotateInGlobals_whenAlsoInPackagedUnit_shouldAnnotate() {
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            unit g {
                symbol = "grm"
                alias_for = 0.001 kg
            }""".trimIndent()
        )
        val element = UnitStubKeyIndex.findUnits(project, "$pkgName.g").first().dataRef
        val mock = AnnotationHolderMock()
        val annotator = LcaAssignmentAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify { mock.holder.newAnnotation(HighlightSeverity.ERROR, "This name is already defined.") }
        verify { mock.builder.range(element) }
        verify { mock.builder.highlightType(ProblemHighlightType.ERROR) }
        verify { mock.builder.create() }
    }

    @Test
    fun testAnnotateInLocals_whenUnique_shouldDoNothing() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            process p {
                variables {
                    x = 1 l
                }
            }
        """.trimIndent()
        )
        val element = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p")
            .first()
            .variablesList
            .first()
            .assignmentList
            .first()
            .getDataRef()
        val mock = AnnotationHolderMock()
        val annotator = LcaAssignmentAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify(exactly = 0) { mock.holder.newAnnotation(any(), any()) }
        verify(exactly = 0) { mock.builder.create() }
    }


    @Test
    fun testAnnotateInLocals_whenDefinedTwice_shouldAnnotate() {
        // given
        val pkgName = {}.javaClass.enclosingMethod.name
        myFixture.createFile(
            "$pkgName.lca", """
            package $pkgName
            
            process p {
                variables {
                    x = 1l
                    x = 2l
                }
            }
        """.trimIndent()
        )
        val element = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p")
            .first()
            .variablesList
            .first()
            .assignmentList
            .first()
            .getDataRef()
        val mock = AnnotationHolderMock()
        val annotator = LcaAssignmentAnnotator()

        // when
        annotator.annotate(element, mock.holder)

        // then
        verify { mock.holder.newAnnotation(HighlightSeverity.ERROR, "This name is already defined.") }
        verify { mock.builder.range(element) }
        verify { mock.builder.highlightType(ProblemHighlightType.ERROR) }
        verify { mock.builder.create() }

    }

}
