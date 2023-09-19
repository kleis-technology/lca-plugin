package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.language.psi.stub.substance.SubstanceStub
import ch.kleis.lcaac.plugin.language.psi.type.PsiSubstance
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType

abstract class PsiSubstanceMixin : StubBasedPsiElementBase<SubstanceStub>, PsiSubstance {
    constructor(node: ASTNode) : super(node)
    constructor(stub: SubstanceStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getName(): String {
        return super<PsiSubstance>.getName()
    }
}
