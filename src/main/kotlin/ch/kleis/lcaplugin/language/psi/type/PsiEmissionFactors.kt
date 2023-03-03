package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

interface PsiEmissionFactors: PsiElement {
    fun getExchanges(): Collection<PsiExplicitExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.EXPLICIT_EXCHANGE))
            .map { it.psi as PsiExplicitExchange }
    }
}
