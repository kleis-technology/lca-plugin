package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.language.psi.type.ref.PsiIndicatorRef
import ch.kleis.lcaplugin.psi.LcaElementTypes

interface PsiImpactExchange : PsiExchange {
    fun getIndicatorRef(): PsiIndicatorRef {
        return node.findChildByType(LcaElementTypes.INDICATOR_REF)?.psi as PsiIndicatorRef
    }
}
