package ch.kleis.lcaplugin.language.psi.type.field

import ch.kleis.lcaplugin.psi.LcaTokenTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

class PsiSubstanceTypeField(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getType(): String {
        return listOfNotNull(
            node.findChildByType(LcaTokenTypes.TYPE_EMISSION_KEYWORD),
            node.findChildByType(LcaTokenTypes.TYPE_RESOURCE_KEYWORD),
            node.findChildByType(LcaTokenTypes.TYPE_LAND_USE_KEYWORD)
        )
            .map { it.psi?.text }
            .firstOrNull() ?: ""
    }
}
