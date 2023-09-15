package ch.kleis.lcaac.plugin.language.psi.mixin.trait

import ch.kleis.lcaac.plugin.language.psi.type.trait.PsiUrnOwner
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiUrnOwnerMixin(node: ASTNode): ASTWrapperPsiElement(node), PsiUrnOwner {
    override fun getName(): String {
        return super<PsiUrnOwner>.getName()
    }
}
