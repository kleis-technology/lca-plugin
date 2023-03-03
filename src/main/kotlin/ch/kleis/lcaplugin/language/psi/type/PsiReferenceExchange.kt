package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.trait.PsiUIDOwner
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnit
import ch.kleis.lcaplugin.psi.LcaTypes
import java.lang.Double.parseDouble

interface PsiReferenceExchange : PsiUIDOwner {
    fun getAmount(): Double {
        return parseDouble(node.findChildByType(LcaTypes.NUMBER)!!.psi.text)
    }

    fun getUnit(): PsiUnit {
        return node.findChildByType(LcaTypes.UNIT)?.psi as PsiUnit
    }
}
