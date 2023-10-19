package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.core.lang.SubstanceKey
import ch.kleis.lcaac.plugin.language.psi.stub.substance.SubstanceStub
import ch.kleis.lcaac.plugin.psi.LcaImpactExchange
import ch.kleis.lcaac.plugin.psi.LcaSubstance
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType

abstract class PsiSubstanceMixin : StubBasedPsiElementBase<SubstanceStub>, LcaSubstance {
    constructor(node: ASTNode) : super(node)
    constructor(stub: SubstanceStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun buildUniqueKey(): SubstanceKey {
        return SubstanceKey(
            this.name,
            getCompartmentField().getValue(),
            getSubCompartmentField()?.getValue(),
            getTypeField().getValue(),
        )
    }

    override fun getName(): String {
        return getSubstanceRef().name
    }

    override fun getNameIdentifier(): PsiElement? {
        return getSubstanceRef().nameIdentifier
    }

    override fun setName(name: String): PsiElement {
        getSubstanceRef().name = name
        return this
    }

    override fun getImpactExchanges(): List<LcaImpactExchange> {
        return getBlockImpactsList()
            .flatMap { it.impactExchangeList }
    }
}
