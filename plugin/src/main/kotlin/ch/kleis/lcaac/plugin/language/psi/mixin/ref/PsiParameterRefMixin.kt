package ch.kleis.lcaac.plugin.language.psi.mixin.ref

import ch.kleis.lcaac.plugin.language.psi.reference.ParameterReference
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiParameterRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiParameterRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiParameterRef {
    override fun getReference(): ParameterReference {
        return super<PsiParameterRef>.getReference()
    }

    override fun getName(): String {
        return super<PsiParameterRef>.getName()
    }
}
