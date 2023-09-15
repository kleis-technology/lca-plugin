package ch.kleis.lcaac.plugin.language.psi.mixin.ref

import ch.kleis.lcaac.plugin.language.psi.reference.LabelReference
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiLabelRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiLabelRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiLabelRef {
    override fun getReference(): LabelReference {
        return super<PsiLabelRef>.getReference()
    }

    override fun getName(): String {
        return super<PsiLabelRef>.getName()
    }
}
