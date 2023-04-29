package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.reference.ProductReference
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

class PsiProductRef(node: ASTNode) : ASTWrapperPsiElement(node), PsiUIDOwner, PsiLcaRef {
    override fun getName(): String {
        return super<PsiUIDOwner>.getName()
    }

    override fun getReference(): ProductReference {
        return ProductReference(this)
    }
}
