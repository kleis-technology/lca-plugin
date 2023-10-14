package ch.kleis.lcaac.plugin.language.ide.insight

import ch.kleis.lcaac.plugin.language.ide.insight.AnnotatorHelper.annotateErrWithMessage
import ch.kleis.lcaac.plugin.psi.LcaProcessTemplateSpec
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement

class LcaProcessTemplateSpecAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is LcaProcessTemplateSpec) {
            return
        }
        val products = element.processRef.reference.resolve()
        if (products == null) {
            val name = element.processRef.uid
            annotateErrWithMessage(
                element,
                holder,
                "Unable to find product $name",
            )
        }
    }
}
