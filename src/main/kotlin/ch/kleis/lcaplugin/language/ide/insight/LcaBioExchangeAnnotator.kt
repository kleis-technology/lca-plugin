package ch.kleis.lcaplugin.language.ide.insight

import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiBioExchange
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

class LcaBioExchangeAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is PsiBioExchange) {
            return
        }

        val target = element.getSubstanceSpec().reference.resolve()
        if (target == null || target !is PsiSubstance) {
            val name = element.getSubstanceSpec().getSubstanceRef().name
            holder.newAnnotation(HighlightSeverity.WARNING, "unresolved substance $name")
                .range(element.getSubstanceSpec())
                .highlightType(ProblemHighlightType.WARNING)
                .create()
        }
    }
}
