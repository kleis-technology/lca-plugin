package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.core.lang.register.DataSourceKey
import ch.kleis.lcaac.plugin.psi.LcaDataSource
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

abstract class PsiDataSourceMixin(node: ASTNode): ASTWrapperPsiElement(node), LcaDataSource {
    override fun buildUniqueKey(): DataSourceKey = DataSourceKey(name)

    override fun getName(): String {
        return dataSourceRef.name
    }

    override fun getNameIdentifier(): PsiElement? {
        return dataSourceRef.nameIdentifier
    }

    override fun setName(name: String): PsiElement {
        dataSourceRef.name = name
        return this
    }
}
