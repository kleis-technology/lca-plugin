package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.language.psi.type.PsiLabelAssignment
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiLabelAssignmentMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiLabelAssignment {
    override fun getName(): String {
        return super<PsiLabelAssignment>.getName()
    }
}
