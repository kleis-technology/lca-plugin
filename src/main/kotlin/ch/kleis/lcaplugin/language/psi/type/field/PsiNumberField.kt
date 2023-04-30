package ch.kleis.lcaplugin.language.psi.type.field

import ch.kleis.lcaplugin.grammar.LcaLangLexer
import ch.kleis.lcaplugin.language.parser.LcaTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import java.lang.Double.parseDouble

class PsiNumberField(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getValue(): Double {
        val number = node.findChildByType(LcaTypes.token(LcaLangLexer.NUMBER))?.psi?.text ?: "1.0"
        return parseDouble(number)
    }
}
