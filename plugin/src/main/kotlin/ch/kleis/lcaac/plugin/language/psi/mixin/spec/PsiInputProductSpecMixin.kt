package ch.kleis.lcaac.plugin.language.psi.mixin.spec

import ch.kleis.lcaac.plugin.language.psi.reference.OutputProductReferenceFromPsiInputProductSpec
import ch.kleis.lcaac.plugin.psi.LcaInputProductSpec
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

abstract class PsiInputProductSpecMixin(node: ASTNode) : ASTWrapperPsiElement(node), LcaInputProductSpec {
    override fun getReference(): OutputProductReferenceFromPsiInputProductSpec {
        return OutputProductReferenceFromPsiInputProductSpec(this)
    }

    override fun getName(): String {
        return getProductRef().name
    }

    override fun setName(name: String): PsiElement {
        getProductRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getProductRef().nameIdentifier
    }
}
