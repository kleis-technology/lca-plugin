package ch.kleis.lcaac.plugin.language.psi.mixin.ref

import ch.kleis.lcaac.plugin.language.psi.reference.ProcessReferenceFromPsiProcessRef
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiProcessRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiProcessRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiProcessRef {
    override fun getReference(): ProcessReferenceFromPsiProcessRef {
        return super<PsiProcessRef>.getReference()
    }

    override fun getName(): String {
        return super<PsiProcessRef>.getName()
    }
}
