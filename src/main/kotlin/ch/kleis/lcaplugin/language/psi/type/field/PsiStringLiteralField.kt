package ch.kleis.lcaplugin.language.psi.type.field

import ch.kleis.lcaplugin.grammar.LcaLangLexer
import ch.kleis.lcaplugin.language.parser.LcaTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

class PsiStringLiteralField(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    fun getValue(): String {
        return node.findChildByType(LcaTypes.token(LcaLangLexer.STRING_LITERAL))?.psi?.text?.trim('"')
            ?: ""
    }
}
