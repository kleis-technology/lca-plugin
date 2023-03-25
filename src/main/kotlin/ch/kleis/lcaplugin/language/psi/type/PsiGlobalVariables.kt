package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

interface PsiGlobalVariables: PsiElement {
    fun getGlobalAssignments(): Collection<PsiGlobalAssignment> {
        return node.getChildren(TokenSet.create(LcaTypes.GLOBAL_ASSIGNMENT))
            .map { it.psi as PsiGlobalAssignment }
    }

    fun getEntries(): Collection<Pair<String, PsiQuantity>> {
        return node.getChildren(TokenSet.create(LcaTypes.ASSIGNMENT))
            .map { it.psi as PsiAssignment }
            .map { Pair(it.getUID().name, it.getValue()) }
    }
}
