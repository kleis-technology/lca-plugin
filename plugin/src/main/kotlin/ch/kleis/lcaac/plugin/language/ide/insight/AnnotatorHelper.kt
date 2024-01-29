package ch.kleis.lcaac.plugin.language.ide.insight

import ch.kleis.lcaac.plugin.language.psi.type.PsiAssignment
import ch.kleis.lcaac.plugin.language.psi.type.PsiColumnDefinition
import ch.kleis.lcaac.plugin.language.psi.type.PsiGlobalAssignment
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiColumnRef
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiDataRef
import ch.kleis.lcaac.plugin.language.psi.type.unit.PsiUnitDefinition
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

object AnnotatorHelper {
    fun annotateWarnWithMessage(element: PsiElement, holder: AnnotationHolder, message: String): Unit =
        holder.newAnnotation(HighlightSeverity.WARNING, message)
            .range(element)
            .highlightType(ProblemHighlightType.WARNING)
            .create()

    fun annotateErrWithMessage(element: PsiElement, holder: AnnotationHolder, message: String): Unit =
        holder.newAnnotation(HighlightSeverity.ERROR, message)
            .range(element)
            .highlightType(ProblemHighlightType.ERROR)
            .create()

    fun isAssignmentReceiver(element: PsiDataRef) =
        isUnitDefName(element) || isLeftHandSideOfGlobalAssignment(element) || isLeftHandSideOfLocalAssignment(element)

    fun isColumnDefinitionReceiver(element: PsiColumnRef) =
        isLeftHandSideOfColumnDefinition(element)

    private fun isUnitDefName(element: PsiDataRef): Boolean =
        element.parent is PsiUnitDefinition

    private fun isLeftHandSideOfGlobalAssignment(element: PsiDataRef): Boolean =
        element.parent is PsiGlobalAssignment && element.nextSibling != null

    private fun isLeftHandSideOfLocalAssignment(element: PsiDataRef): Boolean =
        element.parent is PsiAssignment && element.nextSibling != null

    private fun isLeftHandSideOfColumnDefinition(element: PsiColumnRef): Boolean =
        element.parent is PsiColumnDefinition && element.nextSibling != null
}
