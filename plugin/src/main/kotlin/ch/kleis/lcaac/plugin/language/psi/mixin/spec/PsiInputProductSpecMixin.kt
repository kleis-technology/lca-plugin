package ch.kleis.lcaac.plugin.language.psi.mixin.spec

import ch.kleis.lcaac.plugin.language.psi.reference.OutputProductReferenceFromPsiInputProductSpec
import ch.kleis.lcaac.plugin.language.psi.type.spec.PsiInputProductSpec
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiInputProductSpecMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiInputProductSpec {
    override fun getReference(): OutputProductReferenceFromPsiInputProductSpec {
        return super<PsiInputProductSpec>.getReference()
    }

    override fun getName(): String {
        return super<PsiInputProductSpec>.getName()
    }
}