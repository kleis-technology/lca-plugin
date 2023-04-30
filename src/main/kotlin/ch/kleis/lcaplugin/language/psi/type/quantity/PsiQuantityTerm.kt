package ch.kleis.lcaplugin.language.psi.type.quantity

import ch.kleis.lcaplugin.grammar.LcaLangLexer
import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.enums.MultiplicativeOperationType
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

class PsiQuantityTerm(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getFactor(): PsiQuantityFactor {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_quantityFactor))?.psi as PsiQuantityFactor
    }

    fun getOperationType(): MultiplicativeOperationType? {
        return node.findChildByType(LcaTypes.token(LcaLangLexer.STAR))?.let { MultiplicativeOperationType.MUL }
            ?: node.findChildByType(LcaTypes.token(LcaLangLexer.SLASH))?.let { MultiplicativeOperationType.DIV }
    }

    fun getNext(): PsiQuantityTerm? {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_quantityTerm))?.psi as PsiQuantityTerm?
    }
}
