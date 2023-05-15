package ch.kleis.lcaplugin.language.psi.type.quantity

import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiQuantityMulTerm : PsiElement {
    fun getLeft(): PsiQuantityDivideTerm {
        return node.findChildByType(LcaTypes.QUANTITY_DIVIDE_TERM)?.psi as PsiQuantityDivideTerm
    }

    fun getRight(): PsiQuantityMulTerm? {
        return node.findChildByType(LcaTypes.QUANTITY_MUL_TERM)?.psi as PsiQuantityMulTerm?
    }
}
