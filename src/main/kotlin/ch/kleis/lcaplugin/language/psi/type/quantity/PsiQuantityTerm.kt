package ch.kleis.lcaplugin.language.psi.type.quantity

import ch.kleis.lcaplugin.language.psi.type.enums.MultiplicativeOperationType
import ch.kleis.lcaplugin.psi.LcaElementTypes
import ch.kleis.lcaplugin.psi.LcaTokenTypes
import com.intellij.psi.PsiElement

interface PsiQuantityTerm : PsiElement {
    fun getFactor(): PsiQuantityFactor {
        return node.findChildByType(LcaElementTypes.QUANTITY_FACTOR)?.psi as PsiQuantityFactor
    }

    fun getOperationType(): MultiplicativeOperationType? {
        return node.findChildByType(LcaTokenTypes.STAR)?.let { MultiplicativeOperationType.MUL }
            ?: node.findChildByType(LcaTokenTypes.SLASH)?.let { MultiplicativeOperationType.DIV }
    }

    fun getNext(): PsiQuantityTerm? {
        return node.findChildByType(LcaElementTypes.QUANTITY_TERM)?.psi as PsiQuantityTerm?
    }
}
