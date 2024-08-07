package ch.kleis.lcaac.plugin.language.psi.mixin.ref

import ch.kleis.lcaac.plugin.language.psi.reference.OutputProductReferenceFromPsiProductRef
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiProductRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiProductRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiProductRef {
    override fun getReference(): OutputProductReferenceFromPsiProductRef {
        return super<PsiProductRef>.getReference()
    }

    override fun getName(): String {
        return super<PsiProductRef>.getName()
    }
}
