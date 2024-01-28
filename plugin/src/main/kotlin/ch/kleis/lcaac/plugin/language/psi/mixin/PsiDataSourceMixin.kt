package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.language.psi.stub.datasource.DataSourceStub
import ch.kleis.lcaac.plugin.psi.LcaDataSourceDefinition
import ch.kleis.lcaac.plugin.psi.LcaDataSourceRef
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.util.PsiTreeUtil

abstract class PsiDataSourceMixin : StubBasedPsiElementBase<DataSourceStub>, LcaDataSourceDefinition {
    constructor(node: ASTNode) : super(node)
    constructor(stub: DataSourceStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getDataSourceRef(): LcaDataSourceRef {
        return PsiTreeUtil.findChildrenOfType(this, LcaDataSourceRef::class.java).elementAt(0)
    }

    override fun getName(): String {
        return getDataSourceRef().name
    }

    override fun setName(name: String): PsiElement {
        getDataSourceRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getDataSourceRef().nameIdentifier
    }
}
