package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.psi.LcaColumn
import ch.kleis.lcaac.plugin.psi.LcaDataExpression
import ch.kleis.lcaac.plugin.psi.LcaTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.tree.TokenSet

abstract class PsiColumnDefinitionMixin(node : ASTNode) : ASTWrapperPsiElement(node), LcaColumn {
    override fun getColumnName(): String {
        return node.getChildren(TokenSet.create(LcaTypes.STRING_LITERAL))[0].text.trim('"')
    }
}
