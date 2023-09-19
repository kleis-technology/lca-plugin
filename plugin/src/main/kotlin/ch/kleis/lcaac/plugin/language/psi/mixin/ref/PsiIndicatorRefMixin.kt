package ch.kleis.lcaac.plugin.language.psi.mixin.ref

import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiIndicatorRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiIndicatorRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiIndicatorRef {
    override fun getName(): String {
        return super<PsiIndicatorRef>.getName()
    }
}
