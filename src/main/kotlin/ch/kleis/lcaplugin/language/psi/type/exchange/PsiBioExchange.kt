package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.language.psi.type.ref.PsiSubstanceRef
import ch.kleis.lcaplugin.language.psi.type.spec.PsiSubstanceSpec
import ch.kleis.lcaplugin.psi.LcaTypes

interface PsiBioExchange : PsiExchange {
    fun getSubstanceRef(): PsiSubstanceRef {
        return getSubstanceSpec().getSubstanceRef()
    }

    fun getSubstanceSpec(): PsiSubstanceSpec {
        return node.findChildByType(LcaTypes.SUBSTANCE_SPEC)?.psi as PsiSubstanceSpec
    }
}
