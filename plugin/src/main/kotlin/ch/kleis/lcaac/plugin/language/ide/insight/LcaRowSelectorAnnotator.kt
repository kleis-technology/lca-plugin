package ch.kleis.lcaac.plugin.language.ide.insight

import ch.kleis.lcaac.plugin.language.ide.insight.AnnotatorHelper.annotateErrWithMessage
import ch.kleis.lcaac.plugin.language.psi.type.PsiColumnDefinition
import ch.kleis.lcaac.plugin.language.type_checker.PsiLcaTypeChecker
import ch.kleis.lcaac.plugin.language.type_checker.PsiTypeCheckException
import ch.kleis.lcaac.plugin.psi.LcaRowSelector
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement

// TODO: Test me
class LcaRowSelectorAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is LcaRowSelector) {
            val columnRef = element.columnRef
            val dataExpression = element.dataExpression
            if (dataExpression == null) {
                annotateErrWithMessage(element, holder, "Missing right-hand side")
                return
            }
            val columnDefinition = columnRef.reference.resolve()
            if (columnDefinition !is PsiColumnDefinition) {
                annotateErrWithMessage(element, holder, "Unknown column '${columnRef.name}'")
                return
            }
            val defaultValue = columnDefinition.getValue()
            val checker = PsiLcaTypeChecker()
            val defaultValueType = try {
                checker.check(defaultValue)
            } catch (e: PsiTypeCheckException) {
                annotateErrWithMessage(element, holder, "Could not determine the type of column '${columnRef.name}'")
                return
            }
            val dataType = try {
                checker.check(dataExpression)
            } catch (e: PsiTypeCheckException) {
                annotateErrWithMessage(element, holder, "Could not determine the type of right-hand side")
                return
            }
            if (dataType != defaultValueType) {
                annotateErrWithMessage(element, holder, "Expected ${defaultValueType}, found $dataType")
                return
            }
        }
    }
}
