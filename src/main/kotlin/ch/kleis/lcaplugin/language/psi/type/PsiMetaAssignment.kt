package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.grammar.LcaLangLexer
import ch.kleis.lcaplugin.language.parser.LcaTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

class PsiMetaAssignment(node: ASTNode) : ASTWrapperPsiElement(node), PsiElement {
    override fun getName(): String {
        return getKey()
    }

    private fun getKey(): String {
        return node.getChildren(TokenSet.create(LcaTypes.token(LcaLangLexer.STRING_LITERAL)))[0].text.trim('"')
    }

    fun getValue(): String {
        return node.getChildren(TokenSet.create(LcaTypes.token(LcaLangLexer.STRING_LITERAL)))[1].text.trim('"')
    }
}
