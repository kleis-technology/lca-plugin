package ch.kleis.lcaac.plugin.language.psi.reference

import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiSubstanceRef
import ch.kleis.lcaac.plugin.psi.LcaSubstance
import ch.kleis.lcaac.plugin.psi.LcaSubstanceSpec
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil

class SubstanceReferenceFromPsiSubstanceRef(
    element: PsiSubstanceRef
) : PsiReferenceBase<PsiSubstanceRef>(element) {
    override fun resolve(): PsiElement? {
        return getEnclosingSubstanceSpec(element)?.reference?.resolve()
            ?: getEnclosingSubstance(element)
    }

    private fun getEnclosingSubstanceSpec(element: PsiSubstanceRef): LcaSubstanceSpec? {
        return PsiTreeUtil.getParentOfType(element, LcaSubstanceSpec::class.java)
    }

    private fun getEnclosingSubstance(element: PsiSubstanceRef): LcaSubstance? {
        return PsiTreeUtil.getParentOfType(element, LcaSubstance::class.java)
            ?.takeIf { it.name == element.name }
    }
}
