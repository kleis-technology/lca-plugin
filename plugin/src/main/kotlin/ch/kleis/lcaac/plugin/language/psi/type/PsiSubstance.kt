package ch.kleis.lcaac.plugin.language.psi.type

import ch.kleis.lcaac.core.lang.SubstanceKey
import ch.kleis.lcaac.plugin.language.psi.stub.substance.SubstanceStub
import ch.kleis.lcaac.plugin.language.psi.type.field.PsiStringLiteralField
import ch.kleis.lcaac.plugin.language.psi.type.field.PsiSubstanceTypeField
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiSubstanceRef
import ch.kleis.lcaac.plugin.language.psi.type.trait.BlockMetaOwner
import ch.kleis.lcaac.plugin.psi.LcaBlockImpacts
import ch.kleis.lcaac.plugin.psi.LcaImpactExchange
import ch.kleis.lcaac.plugin.psi.LcaReferenceUnitField
import ch.kleis.lcaac.plugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement

interface PsiSubstance : BlockMetaOwner, PsiNameIdentifierOwner, StubBasedPsiElement<SubstanceStub> {

    fun buildUniqueKey(): SubstanceKey

    fun getImpactExchanges(): List<LcaImpactExchange>
}
