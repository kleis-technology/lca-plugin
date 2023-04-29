package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.language.psi.type.ref.PsiSubstanceRef
import ch.kleis.lcaplugin.psi.LcaElementTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

class PsiBioExchange(node: ASTNode) : ASTWrapperPsiElement(node), PsiExchange {
    fun getSubstanceRef(): PsiSubstanceRef {
        return node.findChildByType(LcaElementTypes.SUBSTANCE_REF)?.psi as PsiSubstanceRef
    }
}
