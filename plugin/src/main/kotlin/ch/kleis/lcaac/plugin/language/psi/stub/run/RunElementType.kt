package ch.kleis.lcaac.plugin.language.psi.stub.run

import ch.kleis.lcaac.plugin.LcaLanguage
import ch.kleis.lcaac.plugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaac.plugin.psi.LcaRun
import ch.kleis.lcaac.plugin.psi.LcaTest
import ch.kleis.lcaac.plugin.psi.impl.LcaRunImpl
import ch.kleis.lcaac.plugin.psi.impl.LcaTestImpl
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*

class RunElementType(debugName: String) : ILightStubElementType<
    RunStub,
    LcaRun,
    >(
        debugName,
        LcaLanguage.INSTANCE
    ) {
    override fun getExternalId(): String {
        return "lca.${super.toString()}"
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): RunStub {
        return RunStubImpl(parentStub as StubElement<LcaRun>, dataStream.readNameString()!!)
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): RunStub {
        throw UnsupportedOperationException("cannot create unit stub from lighter ast node")
    }

    @Suppress("UNCHECKED_CAST")
    override fun createStub(psi: LcaRun, parentStub: StubElement<out PsiElement>?): RunStub {
        val fqn = psi.runRef.getFullyQualifiedName()
        return RunStubImpl(parentStub as StubElement<LcaRun>, fqn)
    }

    override fun createPsi(stub: RunStub): LcaRun {
        return LcaRunImpl(stub, this)
    }

    override fun indexStub(stub: RunStub, sink: IndexSink) {
        sink.occurrence(LcaStubIndexKeys.RUNS, stub.fqn)
    }

    override fun serialize(stub: RunStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.fqn)
    }
}
