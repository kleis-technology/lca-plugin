package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.language.psi.type.PsiExecute
import ch.kleis.lcaac.plugin.psi.LcaTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.tree.TokenSet

abstract class PsiExecuteMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiExecute {
    override fun getName(): String {
        return "execute"
    }

    override fun getCommands(): List<String> {
        val astNodes: Array<ASTNode> = node.getChildren(TokenSet.create(LcaTypes.STRING_LITERAL))
        return astNodes.asList()
            .map { it.text.trim('"') }
    }

    override fun toString(): String {
        return "uid(${this.name})"
    }
}
