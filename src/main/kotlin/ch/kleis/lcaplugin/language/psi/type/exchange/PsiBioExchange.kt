package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.language.psi.type.ref.PsiSubstanceRef
import ch.kleis.lcaplugin.psi.LcaElementTypes

interface PsiBioExchange : PsiExchange {
    fun getSubstanceRef(): PsiSubstanceRef {
        return node.findChildByType(LcaElementTypes.SUBSTANCE_REF)?.psi as PsiSubstanceRef
    }
}
