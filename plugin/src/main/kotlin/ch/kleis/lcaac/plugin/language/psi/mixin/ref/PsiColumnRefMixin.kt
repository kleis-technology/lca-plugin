package ch.kleis.lcaac.plugin.language.psi.mixin.ref

import ch.kleis.lcaac.plugin.language.psi.reference.ColumnReference
import ch.kleis.lcaac.plugin.language.psi.reference.DataReference
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiColumnRef
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiDataRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiColumnRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiColumnRef {
    override fun getReference(): ColumnReference {
        return super<PsiColumnRef>.getReference()
    }

    override fun getName(): String {
        return super<PsiColumnRef>.getName()
    }
}
