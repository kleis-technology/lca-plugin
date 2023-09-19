package ch.kleis.lcaac.plugin.language.psi.type.ref

import ch.kleis.lcaac.plugin.language.psi.reference.LabelReference
import ch.kleis.lcaac.plugin.language.psi.type.trait.PsiUIDOwner

interface PsiLabelRef : PsiUIDOwner {
    override fun getReference(): LabelReference {
        return LabelReference(this)
    }
}
