package ch.kleis.lcaac.plugin.language.psi.type

import ch.kleis.lcaac.plugin.language.psi.stub.datasource.DataSourceStub
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiDataSourceRef
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement

interface PsiDataSourceDefinition : StubBasedPsiElement<DataSourceStub>, PsiNameIdentifierOwner {
    override fun getName(): String
    fun getDataSourceRef(): PsiDataSourceRef
}
