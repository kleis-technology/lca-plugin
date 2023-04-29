package ch.kleis.lcaplugin.language.psi.type.unit

import ch.kleis.lcaplugin.language.psi.type.enums.MultiplicativeOperationType
import ch.kleis.lcaplugin.psi.LcaElementTypes
import ch.kleis.lcaplugin.psi.LcaTokenTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

class PsiUnit(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getFactor(): PsiUnitFactor {
        return node.findChildByType(LcaElementTypes.UNIT_FACTOR)?.psi as PsiUnitFactor
    }

    fun getOperationType(): MultiplicativeOperationType? {
        return node.findChildByType(LcaTokenTypes.STAR)?.let { MultiplicativeOperationType.MUL }
            ?: node.findChildByType(LcaTokenTypes.SLASH)?.let { MultiplicativeOperationType.DIV }
    }

    fun getNext(): PsiUnit? {
        return node.findChildByType(LcaElementTypes.UNIT)?.psi as PsiUnit?
    }
}
