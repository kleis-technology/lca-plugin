package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.ref.PsiSubstanceRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

class PsiBioExchange(node: ASTNode) : ASTWrapperPsiElement(node), PsiExchange {
    fun getSubstanceRef(): PsiSubstanceRef {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_substanceRef))?.psi as PsiSubstanceRef
    }
}
