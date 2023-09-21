package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.language.psi.stub.global_assignment.GlobalAssignmentStub
import ch.kleis.lcaac.plugin.psi.LcaDataExpression
import ch.kleis.lcaac.plugin.psi.LcaDataRef
import ch.kleis.lcaac.plugin.psi.LcaGlobalAssignment
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.util.PsiTreeUtil

abstract class PsiGlobalAssignmentMixin : StubBasedPsiElementBase<GlobalAssignmentStub>, LcaGlobalAssignment {
    constructor(node: ASTNode) : super(node)
    constructor(stub: GlobalAssignmentStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getDataRef(): LcaDataRef {
        return PsiTreeUtil.findChildrenOfType(this, LcaDataRef::class.java).elementAt(0)
    }

    override fun getValue(): LcaDataExpression {
        return PsiTreeUtil.findChildrenOfType(this, LcaDataExpression::class.java).elementAt(1)
    }

    override fun getName(): String {
        return getDataRef().name
    }

    override fun setName(name: String): PsiElement {
        getDataRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getDataRef().nameIdentifier
    }
}
