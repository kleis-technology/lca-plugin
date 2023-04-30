package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.ref.PsiIndicatorRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

class PsiImpactExchange(node: ASTNode): ASTWrapperPsiElement(node), PsiExchange {
    fun getIndicatorRef(): PsiIndicatorRef {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_indicatorRef))?.psi as PsiIndicatorRef
    }
}
