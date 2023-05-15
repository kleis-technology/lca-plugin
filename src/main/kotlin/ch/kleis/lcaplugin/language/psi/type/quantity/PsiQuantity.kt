package ch.kleis.lcaplugin.language.psi.type.quantity

import ch.kleis.lcaplugin.language.psi.type.enums.AdditiveOperationType
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiQuantity : PsiElement {
    fun getTerm(): PsiQuantityMulTerm {
        return node.findChildByType(LcaTypes.QUANTITY_MUL_TERM)?.psi as PsiQuantityMulTerm
    }

    fun getOperationType(): AdditiveOperationType? {
        return node.findChildByType(LcaTypes.PLUS)?.let { AdditiveOperationType.ADD }
            ?: node.findChildByType(LcaTypes.MINUS)?.let { AdditiveOperationType.SUB }
    }

    fun getNext(): PsiQuantity? {
        return node.findChildByType(LcaTypes.QUANTITY)?.psi as PsiQuantity?
    }
}
