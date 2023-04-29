package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.psi.LcaElementTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

class PsiGlobalVariables(node: ASTNode): ASTWrapperPsiElement(node), PsiElement {
    fun getGlobalAssignments(): Collection<PsiGlobalAssignment> {
        return node.getChildren(TokenSet.create(LcaElementTypes.GLOBAL_ASSIGNMENT))
            .map { it.psi as PsiGlobalAssignment }
    }

    fun getEntries(): Collection<Pair<String, PsiQuantity>> {
        return getGlobalAssignments()
            .map { Pair(it.getQuantityRef().name, it.getValue()) }
    }
}
