package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.language.psi.type.PsiAssignment
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiAssignmentMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiAssignment {
    override fun getName(): String {
        return super<PsiAssignment>.getName()
    }
}
