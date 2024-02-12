package ch.kleis.lcaac.plugin.language.ide.insight

import ch.kleis.lcaac.plugin.language.ide.insight.AnnotatorHelper.annotateErrWithMessage
import ch.kleis.lcaac.plugin.psi.LcaSliceExpression
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement

// TODO: Test me
class LcaSliceExpressionAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is LcaSliceExpression) {
            val columnRef = element.columnRef
            if (columnRef == null) {
                annotateErrWithMessage(element, holder, "Missing column reference")
            } else if (columnRef.reference.resolve() == null) {
                annotateErrWithMessage(element, holder, "Unknown column '${columnRef.name}'")
            }
        }
    }
}
