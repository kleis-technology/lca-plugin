package ch.kleis.lcaplugin.language.psi.type.field

import ch.kleis.lcaplugin.psi.LcaTokenTypes
import com.intellij.psi.PsiElement
import java.lang.Double.parseDouble

interface PsiNumberField : PsiElement {
    fun getValue(): Double {
        val number = node.findChildByType(LcaTokenTypes.NUMBER)?.psi?.text ?: "1.0"
        return parseDouble(number)
    }
}
