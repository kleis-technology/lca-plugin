package ch.kleis.lcaac.plugin.language.psi.mixin.unit

import ch.kleis.lcaac.plugin.language.psi.stub.unit.UnitStub
import ch.kleis.lcaac.plugin.language.psi.type.unit.PsiUnitDefinition
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType

abstract class PsiUnitDefinitionMixin : StubBasedPsiElementBase<UnitStub>, PsiUnitDefinition {
    constructor(node: ASTNode) : super(node)
    constructor(stub: UnitStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getName(): String {
        return super<PsiUnitDefinition>.getName()
    }
}