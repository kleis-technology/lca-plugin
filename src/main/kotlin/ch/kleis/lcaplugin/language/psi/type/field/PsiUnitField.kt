package ch.kleis.lcaplugin.language.psi.type.field

import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnit
import ch.kleis.lcaplugin.psi.LcaElementTypes
import com.intellij.psi.PsiElement

interface PsiUnitField : PsiElement {
    fun getValue(): PsiUnit {
        return node.findChildByType(LcaElementTypes.UNIT)?.psi as PsiUnit
    }
}
