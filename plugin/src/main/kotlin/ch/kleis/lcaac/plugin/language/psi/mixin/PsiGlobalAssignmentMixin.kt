package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.language.psi.stub.global_assignment.GlobalAssignmentStub
import ch.kleis.lcaac.plugin.language.psi.type.PsiGlobalAssignment
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType

abstract class PsiGlobalAssignmentMixin : StubBasedPsiElementBase<GlobalAssignmentStub>, PsiGlobalAssignment {
    constructor(node: ASTNode) : super(node)
    constructor(stub: GlobalAssignmentStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getName(): String {
        return super<PsiGlobalAssignment>.getName()
    }
}
