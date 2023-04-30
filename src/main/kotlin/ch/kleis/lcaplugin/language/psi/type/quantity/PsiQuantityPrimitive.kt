package ch.kleis.lcaplugin.language.psi.type.quantity

import ch.kleis.lcaplugin.grammar.LcaLangLexer
import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import java.lang.Double.parseDouble

enum class QuantityPrimitiveType {
    LITERAL, PAREN, QUANTITY_REF
}

class PsiQuantityPrimitive(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getType(): QuantityPrimitiveType {
        return node.findChildByType(LcaTypes.token(LcaLangLexer.NUMBER))?.let { QuantityPrimitiveType.LITERAL }
            ?: node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_quantity))?.let { QuantityPrimitiveType.PAREN }
            ?: QuantityPrimitiveType.QUANTITY_REF
    }

    fun getAmount(): Double {
        return node.findChildByType(LcaTypes.token(LcaLangLexer.NUMBER))?.psi?.text?.let { parseDouble(it) }!!
    }

    fun getQuantityInParen(): PsiQuantity {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_quantity))?.psi as PsiQuantity
    }

    fun getRef(): PsiQuantityRef {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_quantityRef))?.psi as PsiQuantityRef
    }
}
