package ch.kleis.lcaac.plugin.language.psi.type.ref

import ch.kleis.lcaac.plugin.language.psi.reference.DataReference
import ch.kleis.lcaac.plugin.language.psi.type.trait.PsiUIDOwner

interface PsiDataRef : PsiUIDOwner {
    override fun getReference(): DataReference {
        return DataReference(this)
    }
}
