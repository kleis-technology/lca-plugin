package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.language.psi.type.field.PsiAllocateField
import ch.kleis.lcaplugin.psi.LcaElementTypes
import com.intellij.psi.PsiElement

interface PsiTechnoProductExchangeWithAllocateField : PsiElement {
    fun getTechnoProductExchange(): PsiTechnoProductExchange {
        return node.findChildByType(LcaElementTypes.TECHNO_PRODUCT_EXCHANGE)?.psi as PsiTechnoProductExchange
    }
    fun getAllocateField(): PsiAllocateField {
        return node.findChildByType(LcaElementTypes.ALLOCATE_FIELD)?.psi as PsiAllocateField
    }
}
