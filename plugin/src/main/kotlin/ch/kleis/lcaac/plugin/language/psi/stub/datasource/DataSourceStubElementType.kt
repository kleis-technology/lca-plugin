package ch.kleis.lcaac.plugin.language.psi.stub.datasource

import ch.kleis.lcaac.plugin.LcaLanguage
import ch.kleis.lcaac.plugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaac.plugin.psi.LcaDataSourceDefinition
import ch.kleis.lcaac.plugin.psi.impl.LcaDataSourceDefinitionImpl
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*

class DataSourceStubElementType(debugName: String) :
    ILightStubElementType<DataSourceStub, LcaDataSourceDefinition>(debugName, LcaLanguage.INSTANCE) {
    override fun getExternalId(): String {
        return "lca.${super.toString()}"
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): DataSourceStub {
        return DataSourceStubImpl(parentStub as StubElement<LcaDataSourceDefinition>, dataStream.readNameString()!!)
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): DataSourceStub {
        throw UnsupportedOperationException("cannot create process stub from lighter ast node")
    }

    @Suppress("UNCHECKED_CAST")
    override fun createStub(psi: LcaDataSourceDefinition, parentStub: StubElement<out PsiElement>?): DataSourceStub {
        val fqn = psi.getDataSourceRef().getFullyQualifiedName()
        return DataSourceStubImpl(parentStub as StubElement<LcaDataSourceDefinition>, fqn)
    }

    override fun createPsi(stub: DataSourceStub): LcaDataSourceDefinition {
        return LcaDataSourceDefinitionImpl(stub, this)
    }

    override fun indexStub(stub: DataSourceStub, sink: IndexSink) {
        sink.occurrence(LcaStubIndexKeys.DATA_SOURCES, stub.fqn)
    }

    override fun serialize(stub: DataSourceStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.fqn)
    }
}
