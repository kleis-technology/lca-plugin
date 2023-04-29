package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.reference.ProcessReference
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

class PsiProcessTemplateRef(node: ASTNode) : ASTWrapperPsiElement(node), PsiUIDOwner, PsiLcaRef {
    override fun getName(): String {
        return super<PsiUIDOwner>.getName()
    }

    override fun getReference(): ProcessReference {
        return ProcessReference(this)
    }
}
