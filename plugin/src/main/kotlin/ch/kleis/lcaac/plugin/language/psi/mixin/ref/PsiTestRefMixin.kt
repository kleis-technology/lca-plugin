package ch.kleis.lcaac.plugin.language.psi.mixin.ref

import ch.kleis.lcaac.plugin.language.psi.type.trait.PsiUIDOwner
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiTestRefMixin(node : ASTNode) : ASTWrapperPsiElement(node), PsiUIDOwner {
    override fun getName(): String {
        return super<PsiUIDOwner>.getName()
    }
}
