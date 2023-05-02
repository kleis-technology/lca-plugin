package ch.kleis.lcaplugin.language.psi.type.spec

import ch.kleis.lcaplugin.language.psi.type.field.PsiStringLiteralField
import ch.kleis.lcaplugin.language.psi.type.ref.PsiSubstanceRef
import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import ch.kleis.lcaplugin.psi.LcaTypes

interface PsiSubstanceSpec : PsiUIDOwner {
    fun getSubstanceRef(): PsiSubstanceRef {
        return node.findChildByType(LcaTypes.SUBSTANCE_REF)?.psi as PsiSubstanceRef
    }

    fun getTypeField(): PsiStringLiteralField? {
        return node.findChildByType(LcaTypes.TYPE_FIELD)?.psi as PsiStringLiteralField?
    }

    fun getCompartmentField(): PsiStringLiteralField? {
        return node.findChildByType(LcaTypes.COMPARTMENT_FIELD)?.psi as PsiStringLiteralField?
    }

    fun getSubCompartmentField(): PsiStringLiteralField? {
        return node.findChildByType(LcaTypes.SUB_COMPARTMENT_FIELD)?.psi as PsiStringLiteralField?
    }
}
