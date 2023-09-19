package ch.kleis.lcaac.plugin.language.psi.type.ref

import ch.kleis.lcaac.plugin.language.psi.reference.ParameterReference
import ch.kleis.lcaac.plugin.language.psi.type.trait.PsiUIDOwner

interface PsiParameterRef : PsiUIDOwner {
    override fun getReference(): ParameterReference {
        return ParameterReference(this)
    }
}
