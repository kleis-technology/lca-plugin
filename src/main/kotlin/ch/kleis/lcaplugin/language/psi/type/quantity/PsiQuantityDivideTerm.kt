package ch.kleis.lcaplugin.language.psi.type.quantity

import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiQuantityDivideTerm : PsiElement {
    fun getLeft(): PsiQuantityFactor {
        return node.findChildByType(LcaTypes.QUANTITY_FACTOR)?.psi as PsiQuantityFactor
    }

    fun getRight(): PsiQuantityDivideTerm? {
        return node.findChildByType(LcaTypes.QUANTITY_DIVIDE_TERM)?.psi as PsiQuantityDivideTerm?
    }
}
