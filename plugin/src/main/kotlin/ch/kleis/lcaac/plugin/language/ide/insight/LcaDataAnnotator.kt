package ch.kleis.lcaac.plugin.language.ide.insight

import ch.kleis.lcaac.plugin.language.ide.insight.AnnotatorHelper.annotateWarnWithMessage
import ch.kleis.lcaac.plugin.language.ide.insight.AnnotatorHelper.isAssignmentReceiver
import ch.kleis.lcaac.plugin.language.ide.insight.LcaDataAnnotator.ResolveResult.*
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiDataRef
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement

class LcaDataAnnotator : Annotator {
    enum class ResolveResult {
        NORESOLVE, ONERESOLVE, MANYRESOLVE
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is PsiDataRef && !isAssignmentReceiver(element)) {
            val name = element.name
            when (tryResolve(element)) {
                ONERESOLVE -> Unit

                NORESOLVE ->
                    annotateWarnWithMessage(element, holder, "Unresolved quantity reference $name")

                MANYRESOLVE ->
                    annotateWarnWithMessage(element, holder, "Quantity reference $name has several resolution targets")
            }
        }

    }

    private fun tryResolve(psiDataRef: PsiDataRef): ResolveResult {
        return when (psiDataRef.reference.multiResolve(false).size) {
            0 -> NORESOLVE
            1 -> ONERESOLVE
            else -> MANYRESOLVE
        }
    }

}
