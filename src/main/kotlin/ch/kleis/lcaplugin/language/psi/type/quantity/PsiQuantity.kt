package ch.kleis.lcaplugin.language.psi.type.quantity

import ch.kleis.lcaplugin.language.psi.type.enums.AdditiveOperationType
import ch.kleis.lcaplugin.psi.LcaElementTypes
import ch.kleis.lcaplugin.psi.LcaTokenTypes
import com.intellij.psi.PsiElement

interface PsiQuantity : PsiElement {
    fun getTerm(): PsiQuantityTerm {
        return node.findChildByType(LcaElementTypes.QUANTITY_TERM)?.psi as PsiQuantityTerm
    }

    fun getOperationType(): AdditiveOperationType? {
        return node.findChildByType(LcaTokenTypes.PLUS)?.let { AdditiveOperationType.ADD}
            ?: node.findChildByType(LcaTokenTypes.MINUS)?.let { AdditiveOperationType.SUB }
    }

    fun getNext(): PsiQuantity? {
        return node.findChildByType(LcaElementTypes.QUANTITY)?.psi as PsiQuantity?
    }
}
