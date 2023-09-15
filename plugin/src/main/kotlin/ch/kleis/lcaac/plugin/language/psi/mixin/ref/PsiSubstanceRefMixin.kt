package ch.kleis.lcaac.plugin.language.psi.mixin.ref

import ch.kleis.lcaac.plugin.language.psi.reference.SubstanceReferenceFromPsiSubstanceRef
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiSubstanceRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiSubstanceRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiSubstanceRef {
    override fun getReference(): SubstanceReferenceFromPsiSubstanceRef {
        return super<PsiSubstanceRef>.getReference()
    }

    override fun getName(): String {
        return super<PsiSubstanceRef>.getName()
    }
}
