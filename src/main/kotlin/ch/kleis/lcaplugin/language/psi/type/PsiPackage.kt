package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.trait.PsiUrnOwner
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode


class PsiPackage(node: ASTNode) : ASTWrapperPsiElement(node), PsiUrnOwner {
    override fun getName(): String {
        return super<PsiUrnOwner>.getName()
    }
}
