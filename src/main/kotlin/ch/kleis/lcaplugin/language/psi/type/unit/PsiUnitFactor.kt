package ch.kleis.lcaplugin.language.psi.type.unit

import ch.kleis.lcaplugin.psi.LcaElementTypes
import ch.kleis.lcaplugin.psi.LcaTokenTypes
import com.intellij.psi.PsiElement
import java.lang.Double.parseDouble

interface PsiUnitFactor : PsiElement {
    fun getPrimitive(): PsiUnitPrimitive {
        return node.findChildByType(LcaElementTypes.UNIT_PRIMITIVE)?.psi as PsiUnitPrimitive
    }

    fun getExponent(): Double? {
        return node.findChildByType(LcaTokenTypes.NUMBER)?.psi?.text?.let { parseDouble(it) }
    }

}
