package ch.kleis.lcaplugin.language.psi.type.field

import ch.kleis.lcaplugin.psi.LcaTokenTypes
import com.intellij.psi.PsiElement

interface PsiSubstanceTypeField : PsiElement {
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
