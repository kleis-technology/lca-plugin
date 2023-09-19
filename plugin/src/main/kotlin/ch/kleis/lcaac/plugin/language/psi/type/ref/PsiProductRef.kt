package ch.kleis.lcaac.plugin.language.psi.type.ref

import ch.kleis.lcaac.plugin.language.psi.reference.OutputProductReferenceFromPsiProductRef
import ch.kleis.lcaac.plugin.language.psi.type.trait.PsiUIDOwner

interface PsiProductRef : PsiUIDOwner {
    override fun getReference(): OutputProductReferenceFromPsiProductRef {
        return OutputProductReferenceFromPsiProductRef(this)
    }
}
