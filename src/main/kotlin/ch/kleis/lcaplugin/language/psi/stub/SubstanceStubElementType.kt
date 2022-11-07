package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.language.psi.type.Substance
import ch.kleis.lcaplugin.psi.LcaTypes
import ch.kleis.lcaplugin.psi.impl.LcaSubstanceImpl
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.lang.LighterASTTokenNode
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LightTreeUtil
import com.intellij.psi.stubs.*

class SubstanceStubElementType(debugName: String) : ILightStubElementType<SubstanceStub,
        Substance>(debugName, LcaLanguage.INSTANCE) {
    override fun getExternalId(): String = "lca.${super.toString()}"

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): SubstanceStub {
        return SubstanceStubImpl(parentStub as StubElement<Substance>, dataStream.readNameString()!!, dataStream.readNameString()!!, dataStream.readNameString());
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): SubstanceStub {
        val keyNode = LightTreeUtil.firstChildOfType(tree, node, ch.kleis.lcaplugin.psi.LcaTypes.IDENTIFIER) as LighterASTTokenNode
        return SubstanceStubImpl(
            parentStub as StubElement<Substance>,
            tree.charTable.intern(keyNode.text).toString(), "", ""
        );
    }

    override fun createStub(psi: Substance, parentStub: StubElement<out PsiElement>?): SubstanceStub {
        return SubstanceStubImpl(parentStub as StubElement<Substance>, psi.name!!, "", "")
    }

    override fun createPsi(stub: SubstanceStub): Substance {
        return ch.kleis.lcaplugin.psi.impl.LcaSubstanceImpl(stub, this);
    }

    override fun indexStub(stub: SubstanceStub, sink: IndexSink) {
        sink.occurrence(LcaSubIndexKeys.SUBSTANCES, stub.substanceName);
    }

    override fun serialize(stub: SubstanceStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.substanceName);
        dataStream.writeName(stub.compartment);
        if(stub.subCompartment != null) {
            dataStream.writeName(stub.subCompartment);
        }
    }

}