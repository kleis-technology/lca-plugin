package ch.kleis.lcaac.plugin.language.psi.reference

import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiProductRef
import ch.kleis.lcaac.plugin.psi.LcaInputProductSpec
import ch.kleis.lcaac.plugin.psi.LcaOutputProductSpec
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil

class OutputProductReferenceFromPsiProductRef(
    element: PsiProductRef
) : PsiReferenceBase<PsiProductRef>(element) {
    override fun resolve(): PsiElement? {
        return getEnclosingInputProductSpec(element)?.reference?.resolve()
            ?: getEnclosingOutputProductSpec(element)
    }
    
    private fun getEnclosingInputProductSpec(element: PsiProductRef): LcaInputProductSpec? {
        return PsiTreeUtil.getParentOfType(element, LcaInputProductSpec::class.java)
    }

    private fun getEnclosingOutputProductSpec(element: PsiProductRef): LcaOutputProductSpec? {
        return PsiTreeUtil.getParentOfType(element, LcaOutputProductSpec::class.java)
    }

    override fun getVariants(): Array<Any> {
        return getEnclosingInputProductSpec(element)?.reference?.variants
            ?: emptyArray()
    }
}
