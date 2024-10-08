package ch.kleis.lcaac.plugin.language.psi.stub.substance

import ch.kleis.lcaac.plugin.LcaLanguage
import ch.kleis.lcaac.plugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaac.plugin.psi.LcaSubstance
import ch.kleis.lcaac.plugin.psi.impl.LcaSubstanceImpl
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*

class SubstanceStubElementType(debugName: String) : ILightStubElementType<SubstanceStub,
        LcaSubstance>(debugName, LcaLanguage.INSTANCE) {
    override fun getExternalId(): String = "lca.${super.toString()}"

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): SubstanceStub {
        val key = SubstanceKeyDescriptor.INSTANCE.read(dataStream)
        return SubstanceStubImpl(parentStub as StubElement<LcaSubstance>, key)
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): SubstanceStub {
        throw UnsupportedOperationException("cannot create substance stub from lighter ast node")
    }

    @Suppress("UNCHECKED_CAST")
    override fun createStub(psi: LcaSubstance, parentStub: StubElement<out PsiElement>?): SubstanceStub {
        val fqn = psi.getSubstanceRef().getFullyQualifiedName()
        val type = psi.getTypeField().getValue()
        val compartment = psi.getCompartmentField().getValue()
        val subCompartment = psi.getSubCompartmentField()?.getValue()
        val key = SubstanceKey(fqn, type, compartment, subCompartment)
        return SubstanceStubImpl(parentStub as StubElement<LcaSubstance>, key)
    }

    override fun createPsi(stub: SubstanceStub): LcaSubstance {
        return LcaSubstanceImpl(stub, this)
    }

    override fun indexStub(stub: SubstanceStub, sink: IndexSink) {
        sink.occurrence(LcaStubIndexKeys.SUBSTANCES, stub.key)
    }

    override fun serialize(stub: SubstanceStub, dataStream: StubOutputStream) {
        SubstanceKeyDescriptor.INSTANCE.save(dataStream, stub.key)
    }
}
