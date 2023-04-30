package ch.kleis.lcaplugin.language.psi.type.unit

import ch.kleis.lcaplugin.grammar.LcaLangLexer
import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.enums.MultiplicativeOperationType
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

class PsiUnit(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getFactor(): PsiUnitFactor {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_unitFactor))?.psi as PsiUnitFactor
    }

    fun getOperationType(): MultiplicativeOperationType? {
        return node.findChildByType(LcaTypes.token(LcaLangLexer.STAR))?.let { MultiplicativeOperationType.MUL }
            ?: node.findChildByType(LcaTypes.token(LcaLangLexer.SLASH))?.let { MultiplicativeOperationType.DIV }
    }

    fun getNext(): PsiUnit? {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_unit))?.psi as PsiUnit?
    }
}
