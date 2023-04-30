package ch.kleis.lcaplugin.language.psi.type.block

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiBioExchange
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

class PsiBlockResources(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getExchanges(): Collection<PsiBioExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.rule(LcaLangParser.RULE_bioExchange)))
            .map { it.psi as PsiBioExchange }
    }
}
