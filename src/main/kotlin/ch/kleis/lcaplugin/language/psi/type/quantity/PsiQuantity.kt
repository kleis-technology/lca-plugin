package ch.kleis.lcaplugin.language.psi.type.quantity

import ch.kleis.lcaplugin.grammar.LcaLangLexer
import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.enums.AdditiveOperationType
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

class PsiQuantity(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getTerm(): PsiQuantityTerm {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_quantityTerm))?.psi as PsiQuantityTerm
    }

    fun getOperationType(): AdditiveOperationType? {
        return node.findChildByType(LcaTypes.token(LcaLangLexer.PLUS))?.let { AdditiveOperationType.ADD}
            ?: node.findChildByType(LcaTypes.token(LcaLangLexer.MINUS))?.let { AdditiveOperationType.SUB }
    }

    fun getNext(): PsiQuantity? {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_quantity))?.psi as PsiQuantity?
    }
}
