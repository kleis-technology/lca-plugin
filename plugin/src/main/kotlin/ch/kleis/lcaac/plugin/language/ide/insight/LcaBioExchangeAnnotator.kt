package ch.kleis.lcaac.plugin.language.ide.insight

import ch.kleis.lcaac.plugin.language.ide.insight.AnnotatorHelper.annotateErrWithMessage
import ch.kleis.lcaac.plugin.language.ide.insight.AnnotatorHelper.annotateWarnWithMessage
import ch.kleis.lcaac.plugin.language.psi.type.PsiSubstance
import ch.kleis.lcaac.plugin.language.type_checker.PsiLcaTypeChecker
import ch.kleis.lcaac.plugin.language.type_checker.PsiTypeCheckException
import ch.kleis.lcaac.plugin.psi.LcaSubstanceSpec
import ch.kleis.lcaac.plugin.psi.LcaTerminalBioExchange
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement

class LcaBioExchangeAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is LcaTerminalBioExchange) {
            return
        }

        checkReferenceResolution(element, holder)
        checkType(element, holder)
    }

    private fun checkReferenceResolution(element: LcaTerminalBioExchange, holder: AnnotationHolder) {
        val substanceSpec = element.substanceSpec
        if (substanceSpec == null) {
            annotateErrWithMessage(element, holder, "missing substance")
            return
        }
        val target = substanceSpec.reference?.resolve()
        if (target == null || target !is PsiSubstance) {
            annotateWarnWithMessage(substanceSpec, holder, "unresolved substance ${render(substanceSpec)}")
        }
    }

    private fun checkType(element: LcaTerminalBioExchange, holder: AnnotationHolder) {
        val checker = PsiLcaTypeChecker()
        try {
            checker.check(element)
        } catch (e: PsiTypeCheckException) {
            annotateErrWithMessage(element, holder, e.message.orEmpty())
        }
    }

    private fun render(spec: LcaSubstanceSpec): String {
        val compartmentField = spec.getCompartmentField()?.getValue()?.let { """compartment="$it"""" }
        val subCompartmentField = spec.getSubCompartmentField()?.getValue()?.let { """sub_compartment="$it"""" }
        val args = listOfNotNull(
            compartmentField,
            subCompartmentField,
        ).joinToString()
        return if (args.isBlank()) spec.name else "${spec.name}(${args})"
    }
}
