package ch.kleis.lcaac.plugin.language.psi.mixin.unit

import ch.kleis.lcaac.plugin.language.psi.stub.unit.UnitStub
import ch.kleis.lcaac.plugin.language.psi.type.unit.UnitDefinitionType
import ch.kleis.lcaac.plugin.psi.LcaAliasForField
import ch.kleis.lcaac.plugin.psi.LcaUnitDefinition
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.util.PsiTreeUtil

abstract class PsiUnitDefinitionMixin : StubBasedPsiElementBase<UnitStub>, LcaUnitDefinition {
    constructor(node: ASTNode) : super(node)
    constructor(stub: UnitStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun getName(): String {
        return getDataRef().name
    }

    override fun getNameIdentifier(): PsiElement? {
        return getDataRef().nameIdentifier
    }

    override fun setName(name: String): PsiElement {
        getDataRef().name = name
        return this
    }

    override fun getType(): UnitDefinitionType {
        return PsiTreeUtil.getChildOfType(this, LcaAliasForField::class.java)
            ?.let { UnitDefinitionType.ALIAS }
            ?: UnitDefinitionType.LITERAL
    }
}
