package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.stub.global_assignment.GlobalAssignmentStub
import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.IStubElementType

class PsiGlobalAssignment : StubBasedPsiElementBase<GlobalAssignmentStub>, PsiNameIdentifierOwner {
    constructor(node: ASTNode) : super(node)
    constructor(stub: GlobalAssignmentStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    fun getQuantityRef(): PsiQuantityRef {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_quantityRef))?.psi as PsiQuantityRef
    }

    fun getValue(): PsiQuantity {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_quantity))?.psi as PsiQuantity
    }

    override fun getName(): String {
        return getQuantityRef().name
    }

    override fun setName(name: String): PsiElement {
        getQuantityRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getQuantityRef().nameIdentifier
    }
}
