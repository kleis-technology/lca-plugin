package ch.kleis.lcaplugin.language.psi.type.ref

import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

class PsiIndicatorRef(node: ASTNode) : ASTWrapperPsiElement(node), PsiUIDOwner {
    override fun getName(): String {
        return super<PsiUIDOwner>.getName()
    }
}
