package ch.kleis.lcaplugin.language.psi.type.unit

import ch.kleis.lcaplugin.language.psi.stub.unit.UnitStub
import ch.kleis.lcaplugin.language.psi.type.field.PsiAliasForField
import ch.kleis.lcaplugin.language.psi.type.field.PsiStringLiteralField
import ch.kleis.lcaplugin.language.psi.type.ref.PsiUnitRef
import ch.kleis.lcaplugin.psi.LcaElementTypes
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.IStubElementType

enum class UnitDefinitionType {
    LITERAL, ALIAS
}

class PsiUnitDefinition : PsiNameIdentifierOwner, StubBasedPsiElementBase<UnitStub> {
    constructor(node: ASTNode) : super(node)
    constructor(stub: UnitStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    fun getUnitRef(): PsiUnitRef {
        return node.findChildByType(LcaElementTypes.UNIT_REF)?.psi as PsiUnitRef
    }

    override fun getName(): String {
        return getUnitRef().name
    }

    override fun getNameIdentifier(): PsiElement? {
        return getUnitRef().nameIdentifier
    }

    override fun setName(name: String): PsiElement {
        getUnitRef().name = name
        return this
    }

    fun getSymbolField(): PsiStringLiteralField {
        return node.findChildByType(LcaElementTypes.SYMBOL_FIELD)?.psi as PsiStringLiteralField
    }

    fun getDimensionField(): PsiStringLiteralField {
        return node.findChildByType(LcaElementTypes.DIM_FIELD)?.psi as PsiStringLiteralField
    }

    fun getAliasForField(): PsiAliasForField {
        return node.findChildByType(LcaElementTypes.ALIAS_FOR_FIELD)?.psi as PsiAliasForField
    }

    fun getType(): UnitDefinitionType {
        return node.findChildByType(LcaElementTypes.ALIAS_FOR_FIELD)?.let { UnitDefinitionType.ALIAS }
            ?: UnitDefinitionType.LITERAL
    }
}
