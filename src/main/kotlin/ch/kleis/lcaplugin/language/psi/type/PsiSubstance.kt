package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.field.PsiUnitField
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import ch.kleis.lcaplugin.psi.LcaTypes

interface PsiSubstance: PsiUIDOwner {
    fun getReferenceUnitField(): PsiUnitField {
        return node.findChildByType(LcaTypes.REFERENCE_UNIT_FIELD)?.psi as PsiUnitField
    }

    fun hasExchanges(): Boolean {
        return node.findChildByType(LcaTypes.EMISSION_FACTORS) != null
    }

    fun getExchanges(): Collection<PsiExplicitExchange> {
        return getEFsBlock()?.getExchanges()
            ?: emptyList()
    }

    private fun getEFsBlock(): PsiEmissionFactors? {
        return node.findChildByType(LcaTypes.EMISSION_FACTORS)?.psi as PsiEmissionFactors?
    }
}
