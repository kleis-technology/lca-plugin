package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.language.psi.type.PsiMetaAssignment
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiMetaAssignmentMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiMetaAssignment {
    override fun getName(): String? {
        return getKey()
    }
}
