package ch.kleis.lcaplugin.language.psi.type.field

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.psi.LcaElementTypes
import com.intellij.psi.PsiElement

interface PsiAllocateField : PsiElement {
    fun getValue(): PsiQuantity {
        return node.findChildByType(LcaElementTypes.QUANTITY)?.psi as PsiQuantity
    }
}
