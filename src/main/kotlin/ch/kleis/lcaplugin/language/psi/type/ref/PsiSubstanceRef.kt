package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.reference.SubstanceReference
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

class PsiSubstanceRef(node: ASTNode) : ASTWrapperPsiElement(node), PsiUIDOwner, PsiLcaRef {
    override fun getName(): String {
        return super<PsiUIDOwner>.getName()
    }

    override fun getReference(): SubstanceReference {
        return SubstanceReference(this)
    }
}
