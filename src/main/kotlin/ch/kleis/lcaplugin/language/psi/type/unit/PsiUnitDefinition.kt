package ch.kleis.lcaplugin.language.psi.type.unit

import ch.kleis.lcaplugin.grammar.LcaLangParser
import ch.kleis.lcaplugin.language.parser.LcaTypes
import ch.kleis.lcaplugin.language.psi.stub.unit.UnitStub
import ch.kleis.lcaplugin.language.psi.type.field.PsiAliasForField
import ch.kleis.lcaplugin.language.psi.type.field.PsiStringLiteralField
import ch.kleis.lcaplugin.language.psi.type.ref.PsiUnitRef
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
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_unitRef))?.psi as PsiUnitRef
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
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_symbolField))?.psi as PsiStringLiteralField
    }

    fun getDimensionField(): PsiStringLiteralField {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_dimField))?.psi as PsiStringLiteralField
    }

    fun getAliasForField(): PsiAliasForField {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_aliasForField))?.psi as PsiAliasForField
    }

    fun getType(): UnitDefinitionType {
        return node.findChildByType(LcaTypes.rule(LcaLangParser.RULE_aliasForField))?.let { UnitDefinitionType.ALIAS }
            ?: UnitDefinitionType.LITERAL
    }
}
