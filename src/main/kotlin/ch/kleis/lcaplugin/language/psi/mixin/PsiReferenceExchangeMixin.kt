package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiReferenceExchange
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiReferenceExchangeMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiReferenceExchange {
    override fun getName(): String? {
        return super<PsiReferenceExchange>.getName()
    }
}
