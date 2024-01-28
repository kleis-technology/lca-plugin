package ch.kleis.lcaac.plugin.language.psi.mixin.ref

import ch.kleis.lcaac.plugin.language.psi.reference.DataSourceReference
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiDataSourceRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiDataSourceRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiDataSourceRef {
    override fun getReference(): DataSourceReference {
        return super<PsiDataSourceRef>.getReference()
    }

    override fun getName(): String {
        return super<PsiDataSourceRef>.getName()
    }
}
