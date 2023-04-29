package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.reference.SubstanceReference
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner

interface PsiSubstanceRef : PsiUIDOwner, PsiLcaRef {
    override fun getReference(): SubstanceReference {
        return SubstanceReference(this)
    }
}
