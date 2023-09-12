package ch.kleis.lcaac.plugin.language.psi.stub.unit

import ch.kleis.lcaac.plugin.LcaLanguage
import ch.kleis.lcaac.plugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaac.plugin.psi.LcaUnitDefinition
import ch.kleis.lcaac.plugin.psi.impl.LcaUnitDefinitionImpl
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*

class UnitElementType(debugName: String) : ILightStubElementType<
        UnitStub,
        LcaUnitDefinition
        >(
    debugName,
    LcaLanguage.INSTANCE
) {
    override fun getExternalId(): String {
        return "lca.${super.toString()}"
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): UnitStub {
        return UnitStubImpl(parentStub as StubElement<LcaUnitDefinition>, dataStream.readNameString()!!)
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): UnitStub {
        throw UnsupportedOperationException("cannot create unit stub from lighter ast node")
    }

    @Suppress("UNCHECKED_CAST")
    override fun createStub(psi: LcaUnitDefinition, parentStub: StubElement<out PsiElement>?): UnitStub {
        val fqn = psi.getDataRef().getFullyQualifiedName()
        return UnitStubImpl(parentStub as StubElement<LcaUnitDefinition>, fqn)
    }

    override fun createPsi(stub: UnitStub): LcaUnitDefinition {
        return LcaUnitDefinitionImpl(stub, this)
    }

    override fun indexStub(stub: UnitStub, sink: IndexSink) {
        sink.occurrence(LcaStubIndexKeys.UNITS, stub.fqn)
    }

    override fun serialize(stub: UnitStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.fqn)
    }
}
