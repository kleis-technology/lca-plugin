package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.psi.LcaElementTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

interface PsiParameters: PsiElement {
    fun getAssignments(): Collection<PsiAssignment> {
        return node.getChildren(TokenSet.create(LcaElementTypes.ASSIGNMENT))
            .map { it.psi as PsiAssignment }
    }

    fun getEntries(): Collection<Pair<String, PsiQuantity>> {
        return node.getChildren(TokenSet.create(LcaElementTypes.ASSIGNMENT))
            .map { it.psi as PsiAssignment }
            .map { Pair(it.getQuantityRef().name, it.getValue()) }
    }
}
