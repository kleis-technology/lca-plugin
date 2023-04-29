package ch.kleis.lcaplugin.language.psi.type.block

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaLangTokenSets
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchangeWithAllocateField
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

class PsiBlockProducts(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getExchanges(): Collection<PsiTechnoProductExchange> {
        return node.getChildren(LcaLangTokenSets.create(LcaLangParser.RULE_technoProductExchange))
            .map { it.psi as PsiTechnoProductExchange }
    }
    fun getExchangesWithAllocateField(): Collection<PsiTechnoProductExchangeWithAllocateField> {
        return node.getChildren(LcaLangTokenSets.create(LcaLangParser.RULE_technoProductExchangeWithAllocateField))
            .map { it.psi as PsiTechnoProductExchangeWithAllocateField }
    }
}
