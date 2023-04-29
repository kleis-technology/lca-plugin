package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.trait.PsiUrnOwner
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

class PsiImport(node: ASTNode) : ASTWrapperPsiElement(node), PsiUrnOwner {
    fun getPackageName(): String {
        val parts = getUrn().getParts()
        return parts.joinToString(".")
    }

    override fun getName(): String {
        return super<PsiUrnOwner>.getName()
    }
}
