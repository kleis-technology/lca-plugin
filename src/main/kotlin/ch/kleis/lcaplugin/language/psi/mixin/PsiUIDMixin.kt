package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiUID
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiUIDMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiUID {
    override fun getName(): String {
        return super<PsiUID>.getName()
    }

    override fun toString(): String {
        return "uid(${this.name})"
    }
}
