package ch.kleis.lcaac.plugin.language.ide.insight

import ch.kleis.lcaac.plugin.language.ide.insight.AnnotatorHelper.annotateErrWithMessage
import ch.kleis.lcaac.plugin.language.ide.insight.AnnotatorHelper.annotateWarnWithMessage
import ch.kleis.lcaac.plugin.language.psi.reference.OutputProductReferenceFromPsiInputProductSpec
import ch.kleis.lcaac.plugin.language.type_checker.LcaMatchLabelsEvaluator
import ch.kleis.lcaac.plugin.language.type_checker.PsiLcaTypeChecker
import ch.kleis.lcaac.plugin.language.type_checker.PsiTypeCheckException
import ch.kleis.lcaac.plugin.psi.LcaInputProductSpec
import ch.kleis.lcaac.plugin.psi.LcaOutputProductSpec
import ch.kleis.lcaac.plugin.psi.LcaTerminalTechnoInputExchange
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement

class LcaTerminalTechnoInputExchangeAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is LcaTerminalTechnoInputExchange) {
            return
        }
        val inputProductSpec = element.inputProductSpec
        if (inputProductSpec == null) {
            annotateErrWithMessage(element, holder, "missing input product")
            return
        }
        val targets =
            (inputProductSpec.reference as OutputProductReferenceFromPsiInputProductSpec)
                .multiResolve(false)
                .filter { it.element is LcaOutputProductSpec }

        when (targets.size) {
            0 -> {
                val specString = specToStr(inputProductSpec)
                annotateWarnWithMessage(inputProductSpec, holder, "Could not resolve $specString")
            }
            1 -> {
                val checker = PsiLcaTypeChecker()
                try {
                    checker.check(element)
                } catch (e: PsiTypeCheckException) {
                    annotateErrWithMessage(element, holder, e.message.orEmpty())
                }
            }
            else -> {
                val specString = specToStr(inputProductSpec)
                annotateWarnWithMessage(inputProductSpec, holder, "Multiple candidates found for $specString")
            }
        }
    }

    private fun specToStr(inputProductSpec: LcaInputProductSpec): String {
        val product = inputProductSpec.name
        val process = inputProductSpec.getProcessTemplateSpec()?.name
        val labels = inputProductSpec.getProcessTemplateSpec()
            ?.getMatchLabels()
            ?.let { LcaMatchLabelsEvaluator().evalOrNull(it) }
        return listOfNotNull(
            product,
            process?.let { "from $it" },
            labels?.let { "match $it" },
        ).joinToString(" ")
    }
}
