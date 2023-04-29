package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.language.psi.type.PsiFromProcessConstraint
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProductRef
import ch.kleis.lcaplugin.psi.LcaElementTypes

interface PsiTechnoInputExchange : PsiExchange {
    fun getProductRef(): PsiProductRef {
        return node.findChildByType(LcaElementTypes.PRODUCT_REF)?.psi as PsiProductRef
    }

    fun getFromProcessConstraint(): PsiFromProcessConstraint? {
        return node.findChildByType(LcaElementTypes.FROM_PROCESS_CONSTRAINT)?.psi as PsiFromProcessConstraint?
    }
}
