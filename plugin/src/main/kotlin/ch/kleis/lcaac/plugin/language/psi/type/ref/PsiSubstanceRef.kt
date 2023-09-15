package ch.kleis.lcaac.plugin.language.psi.type.ref

import ch.kleis.lcaac.plugin.language.psi.reference.SubstanceReferenceFromPsiSubstanceRef
import ch.kleis.lcaac.plugin.language.psi.type.trait.PsiUIDOwner

interface PsiSubstanceRef : PsiUIDOwner {
    override fun getReference(): SubstanceReferenceFromPsiSubstanceRef {
        return SubstanceReferenceFromPsiSubstanceRef(this)
    }
}
