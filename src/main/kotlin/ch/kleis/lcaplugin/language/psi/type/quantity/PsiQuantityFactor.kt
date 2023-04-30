package ch.kleis.lcaplugin.language.psi.type.quantity

import ch.kleis.lcaplugin.grammar.LcaLangLexer
import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

class PsiQuantityFactor(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getPrimitive(): PsiQuantityPrimitive {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_quantityPrimitive))?.psi as PsiQuantityPrimitive
    }

    fun getExponent(): Double? {
        return node.findChildByType(LcaTypes.token(LcaLangLexer.NUMBER))?.psi?.text?.let { java.lang.Double.parseDouble(it) }
    }
}
