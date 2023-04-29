package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.reference.ParameterReference
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

class PsiParameterRef(node: ASTNode) : ASTWrapperPsiElement(node), PsiUIDOwner, PsiLcaRef {
    override fun getReference(): ParameterReference {
        return ParameterReference(this)
    }

    override fun getName(): String {
        return super<PsiUIDOwner>.getName()
    }
}
