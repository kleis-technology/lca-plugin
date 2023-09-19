package ch.kleis.lcaac.plugin.language.psi.type

import ch.kleis.lcaac.plugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

interface PsiMetaAssignment : PsiElement {

    fun getName(): String? {
        return getKey()
    }

    fun getKey(): String {
        return node.getChildren(TokenSet.create(LcaTypes.STRING_LITERAL))[0].text.trim('"')
    }

    fun getValue(): String {
        return node.getChildren(TokenSet.create(LcaTypes.STRING_LITERAL))[1].text.trim('"')
    }
}
