package ch.kleis.lcaplugin.language.psi.type.field

import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnit
import ch.kleis.lcaplugin.psi.LcaElementTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

class PsiUnitField(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getValue(): PsiUnit {
        return node.findChildByType(LcaElementTypes.UNIT)?.psi as PsiUnit
    }
}
