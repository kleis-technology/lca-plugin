package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.psi.LcaColumnDefinition
import ch.kleis.lcaac.plugin.psi.LcaColumnRef
import ch.kleis.lcaac.plugin.psi.LcaDataExpression
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

abstract class PsiColumnDefinitionMixin(node: ASTNode) : ASTWrapperPsiElement(node), LcaColumnDefinition {
    override fun getColumnRef(): LcaColumnRef {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, LcaColumnRef::class.java).elementAt(0)
    }

    override fun getValue(): LcaDataExpression {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, LcaDataExpression::class.java).elementAt(0)
    }

    override fun getName(): String {
        return getColumnRef().name
    }

    override fun setName(name: String): PsiElement {
        getColumnRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getColumnRef().nameIdentifier
    }
}
