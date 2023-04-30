package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

class PsiParameters(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getAssignments(): Collection<PsiAssignment> {
        return node.getChildren(TokenSet.create(LcaTypes.rule(LcaLangParser.RULE_assignment)))
            .map { it.psi as PsiAssignment }
    }

    fun getEntries(): Collection<Pair<String, PsiQuantity>> {
        return node.getChildren(TokenSet.create(LcaTypes.rule(LcaLangParser.RULE_assignment)))
            .map { it.psi as PsiAssignment }
            .map { Pair(it.getQuantityRef().name, it.getValue()) }
    }
}
