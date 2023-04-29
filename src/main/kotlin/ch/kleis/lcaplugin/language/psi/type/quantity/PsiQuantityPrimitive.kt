package ch.kleis.lcaplugin.language.psi.type.quantity

import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import ch.kleis.lcaplugin.psi.LcaElementTypes
import ch.kleis.lcaplugin.psi.LcaTokenTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import java.lang.Double.parseDouble

enum class QuantityPrimitiveType {
    LITERAL, PAREN, QUANTITY_REF
}

class PsiQuantityPrimitive(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getType(): QuantityPrimitiveType {
        return node.findChildByType(LcaTokenTypes.NUMBER)?.let { QuantityPrimitiveType.LITERAL }
            ?: node.findChildByType(LcaElementTypes.QUANTITY)?.let { QuantityPrimitiveType.PAREN }
            ?: QuantityPrimitiveType.QUANTITY_REF
    }

    fun getAmount(): Double {
        return node.findChildByType(LcaTokenTypes.NUMBER)?.psi?.text?.let { parseDouble(it) }!!
    }

    fun getQuantityInParen(): PsiQuantity {
        return node.findChildByType(LcaElementTypes.QUANTITY)?.psi as PsiQuantity
    }

    fun getRef(): PsiQuantityRef {
        return node.findChildByType(LcaElementTypes.QUANTITY_REF)?.psi as PsiQuantityRef
    }
}
