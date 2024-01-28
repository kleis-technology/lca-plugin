package ch.kleis.lcaac.plugin.language.psi.type.ref

import ch.kleis.lcaac.plugin.language.psi.reference.ColumnReference
import ch.kleis.lcaac.plugin.language.psi.type.trait.PsiUIDOwner

interface PsiColumnRef : PsiUIDOwner {
    override fun getReference(): ColumnReference {
        return ColumnReference(this)
    }
}
