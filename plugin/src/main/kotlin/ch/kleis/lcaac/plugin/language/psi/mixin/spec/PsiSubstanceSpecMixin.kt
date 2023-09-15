package ch.kleis.lcaac.plugin.language.psi.mixin.spec

import ch.kleis.lcaac.plugin.language.psi.reference.SubstanceReferenceFromPsiSubstanceSpec
import ch.kleis.lcaac.plugin.language.psi.type.spec.PsiSubstanceSpec
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiSubstanceSpecMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiSubstanceSpec {
    override fun getReference(): SubstanceReferenceFromPsiSubstanceSpec {
        return super<PsiSubstanceSpec>.getReference()
    }

    override fun getName(): String {
        return super<PsiSubstanceSpec>.getName()
    }
}
