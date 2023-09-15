package ch.kleis.lcaac.plugin.language.psi.type.ref

import ch.kleis.lcaac.plugin.language.psi.reference.ProcessReferenceFromPsiProcessRef
import ch.kleis.lcaac.plugin.language.psi.type.trait.PsiUIDOwner

interface PsiProcessRef : PsiUIDOwner {
    override fun getReference(): ProcessReferenceFromPsiProcessRef {
        return ProcessReferenceFromPsiProcessRef(this)
    }
}
