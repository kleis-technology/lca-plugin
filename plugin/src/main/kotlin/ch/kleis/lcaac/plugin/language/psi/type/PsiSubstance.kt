package ch.kleis.lcaac.plugin.language.psi.type

import ch.kleis.lcaac.core.lang.register.SubstanceKey
import ch.kleis.lcaac.plugin.language.psi.stub.substance.SubstanceStub
import ch.kleis.lcaac.plugin.language.psi.type.trait.BlockMetaOwner
import ch.kleis.lcaac.plugin.psi.LcaImpactExchange
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement

interface PsiSubstance : BlockMetaOwner, PsiNameIdentifierOwner, StubBasedPsiElement<SubstanceStub> {

    fun buildUniqueKey(): SubstanceKey

    fun getImpactExchanges(): List<LcaImpactExchange>
}
