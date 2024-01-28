package ch.kleis.lcaac.plugin.language.psi.type.ref

import ch.kleis.lcaac.plugin.language.psi.reference.DataSourceReference
import ch.kleis.lcaac.plugin.language.psi.type.trait.PsiUIDOwner

interface PsiDataSourceRef : PsiUIDOwner {
    override fun getReference(): DataSourceReference {
        return DataSourceReference(this)
    }
}
