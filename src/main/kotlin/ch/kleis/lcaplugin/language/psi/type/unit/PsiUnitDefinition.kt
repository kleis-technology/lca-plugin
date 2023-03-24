package ch.kleis.lcaplugin.language.psi.type.unit

import ch.kleis.lcaplugin.language.psi.stub.unit.UnitStub
import ch.kleis.lcaplugin.language.psi.type.field.PsiAliasForField
import ch.kleis.lcaplugin.language.psi.type.field.PsiNumberField
import ch.kleis.lcaplugin.language.psi.type.field.PsiStringLiteralField
import ch.kleis.lcaplugin.language.psi.type.ref.PsiUnitRef
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.StubBasedPsiElement

interface PsiUnitDefinition : PsiNameIdentifierOwner, StubBasedPsiElement<UnitStub> {
    fun getUnitRef(): PsiUnitRef {
        return node.findChildByType(LcaTypes.UNIT_REF)?.psi as PsiUnitRef
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
        return node.findChildByType(LcaTypes.SYMBOL_FIELD)?.psi as PsiStringLiteralField
    }

    fun getDimensionField(): PsiStringLiteralField {
        return node.findChildByType(LcaTypes.DIM_FIELD)?.psi as PsiStringLiteralField
    }

    fun getScaleField(): PsiNumberField {
        return node.findChildByType(LcaTypes.SCALE_FIELD)?.psi as PsiNumberField
    }

    fun getAliasForField(): PsiAliasForField {
        return node.findChildByType(LcaTypes.ALIAS_FOR_FIELD)?.psi as PsiAliasForField
    }

    fun isAlias(): Boolean {
        return node.findChildByType(LcaTypes.ALIAS_FOR_FIELD) != null
    }
}