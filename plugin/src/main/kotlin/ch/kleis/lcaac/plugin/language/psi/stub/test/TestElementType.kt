package ch.kleis.lcaac.plugin.language.psi.stub.test

import ch.kleis.lcaac.plugin.LcaLanguage
import ch.kleis.lcaac.plugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaac.plugin.psi.LcaTest
import ch.kleis.lcaac.plugin.psi.impl.LcaTestImpl
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*

class TestElementType(debugName: String) : ILightStubElementType<
    TestStub,
    LcaTest,
    >(
        debugName,
        LcaLanguage.INSTANCE
    ) {
    override fun getExternalId(): String {
        return "lca.${super.toString()}"
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): TestStub {
        return TestStubImpl(parentStub as StubElement<LcaTest>, dataStream.readNameString()!!)
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): TestStub {
        throw UnsupportedOperationException("cannot create unit stub from lighter ast node")
    }

    @Suppress("UNCHECKED_CAST")
    override fun createStub(psi: LcaTest, parentStub: StubElement<out PsiElement>?): TestStub {
        val fqn = psi.testRef.getFullyQualifiedName()
        return TestStubImpl(parentStub as StubElement<LcaTest>, fqn)
    }

    override fun createPsi(stub: TestStub): LcaTest {
        return LcaTestImpl(stub, this)
    }

    override fun indexStub(stub: TestStub, sink: IndexSink) {
        sink.occurrence(LcaStubIndexKeys.TESTS, stub.fqn)
    }

    override fun serialize(stub: TestStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.fqn)
    }
}
