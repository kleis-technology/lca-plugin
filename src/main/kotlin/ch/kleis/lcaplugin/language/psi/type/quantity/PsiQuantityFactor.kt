package ch.kleis.lcaplugin.language.psi.type.quantity

import ch.kleis.lcaplugin.psi.LcaElementTypes
import ch.kleis.lcaplugin.psi.LcaTokenTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

class PsiQuantityFactor(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getPrimitive(): PsiQuantityPrimitive {
        return node.findChildByType(LcaElementTypes.QUANTITY_PRIMITIVE)?.psi as PsiQuantityPrimitive
    }

    fun getExponent(): Double? {
        return node.findChildByType(LcaTokenTypes.NUMBER)?.psi?.text?.let { java.lang.Double.parseDouble(it) }
    }
}
