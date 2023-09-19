package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.language.psi.type.PsiUID
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
