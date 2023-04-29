package ch.kleis.lcaplugin.language.psi.type.unit

import ch.kleis.lcaplugin.language.psi.type.ref.PsiUnitRef
import ch.kleis.lcaplugin.psi.LcaElementTypes
import ch.kleis.lcaplugin.psi.LcaTokenTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

enum class UnitPrimitiveType {
    DEFINITION, PAREN, VARIABLE
}

class PsiUnitPrimitive(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getType(): UnitPrimitiveType {
        return node.findChildByType(LcaElementTypes.UNIT_DEFINITION)?.let { UnitPrimitiveType.DEFINITION }
            ?: node.findChildByType(LcaTokenTypes.LPAREN)?.let { UnitPrimitiveType.PAREN }
            ?: UnitPrimitiveType.VARIABLE
    }

    fun getDefinition(): PsiUnitDefinition {
        return node.findChildByType(LcaElementTypes.UNIT_DEFINITION)?.psi as PsiUnitDefinition
    }

    fun getUnitInParen(): PsiUnit {
        return node.findChildByType(LcaElementTypes.UNIT)?.psi as PsiUnit
    }

    fun getRef(): PsiUnitRef {
        return node.findChildByType(LcaElementTypes.UNIT_REF)?.psi as PsiUnitRef
    }
}
