package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.psi.LcaMetaAssignment
import ch.kleis.lcaac.plugin.psi.LcaTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.tree.TokenSet

abstract class PsiMetaAssignmentMixin(node: ASTNode) : ASTWrapperPsiElement(node), LcaMetaAssignment {
    override fun getName(): String? {
        return getKey()
    }

    override fun getKey(): String {
        return node.getChildren(TokenSet.create(LcaTypes.STRING_LITERAL))[0].text.trim('"')
    }

    override fun getValue(): String {
        return node.getChildren(TokenSet.create(LcaTypes.STRING_LITERAL))[1].text.trim('"')
    }
}
