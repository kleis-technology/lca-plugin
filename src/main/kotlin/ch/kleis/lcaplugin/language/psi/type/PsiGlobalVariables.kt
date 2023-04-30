package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

class PsiGlobalVariables(node: ASTNode): ASTWrapperPsiElement(node), PsiElement {
    fun getGlobalAssignments(): Collection<PsiGlobalAssignment> {
        return node.getChildren(TokenSet.create(LcaTypes.rule(LcaLangParser.RULE_globalAssignment)))
            .map { it.psi as PsiGlobalAssignment }
    }

    fun getEntries(): Collection<Pair<String, PsiQuantity>> {
        return getGlobalAssignments()
            .map { Pair(it.getQuantityRef().name, it.getValue()) }
    }
}
