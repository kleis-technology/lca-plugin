package ch.kleis.lcaplugin.language.psi.type.unit

import ch.kleis.lcaplugin.psi.LcaElementTypes
import ch.kleis.lcaplugin.psi.LcaTokenTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import java.lang.Double.parseDouble

class PsiUnitFactor(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getPrimitive(): PsiUnitPrimitive {
        return node.findChildByType(LcaElementTypes.UNIT_PRIMITIVE)?.psi as PsiUnitPrimitive
    }

    fun getExponent(): Double? {
        return node.findChildByType(LcaTokenTypes.NUMBER)?.psi?.text?.let { parseDouble(it) }
    }

}
