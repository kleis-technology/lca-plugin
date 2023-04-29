package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.language.psi.type.ref.PsiIndicatorRef
import ch.kleis.lcaplugin.psi.LcaElementTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

class PsiImpactExchange(node: ASTNode): ASTWrapperPsiElement(node), PsiExchange {
    fun getIndicatorRef(): PsiIndicatorRef {
        return node.findChildByType(LcaElementTypes.INDICATOR_REF)?.psi as PsiIndicatorRef
    }
}
