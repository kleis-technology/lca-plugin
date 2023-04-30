package ch.kleis.lcaplugin.language.psi.type.unit

import ch.kleis.lcaplugin.grammar.LcaLangLexer
import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import java.lang.Double.parseDouble

class PsiUnitFactor(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getPrimitive(): PsiUnitPrimitive {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_unitPrimitive))?.psi as PsiUnitPrimitive
    }

    fun getExponent(): Double? {
        return node.findChildByType(LcaTypes.token(LcaLangLexer.NUMBER))?.psi?.text?.let { parseDouble(it) }
    }

}
