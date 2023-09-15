package ch.kleis.lcaac.plugin.language.psi.type.field

import ch.kleis.lcaac.plugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiStringLiteralField : PsiElement {
    fun getValue(): String {
        return node.findChildByType(LcaTypes.STRING_LITERAL)?.psi?.text?.trim('"')
            ?: ""
    }
}
