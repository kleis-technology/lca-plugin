package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiExplicitExchange
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiExplicitExchangeMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiExplicitExchange {
    override fun getName(): String? {
        return super<PsiExplicitExchange>.getName()
    }
}
