package ch.kleis.lcaac.plugin.language.ide.insight

import ch.kleis.lcaac.plugin.language.ide.insight.AnnotatorHelper.annotateErrWithMessage
import ch.kleis.lcaac.plugin.language.ide.insight.AnnotatorHelper.isColumnDefinitionReceiver
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiColumnRef
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement

// TODO: Test me
class LcaColumnDefinitionAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is PsiColumnRef && isColumnDefinitionReceiver(element)) {
            if (element.reference.multiResolve(false).size > 1) {
                annotateErrWithMessage(element, holder, "This name is already defined.")
            }
        }
    }
}
