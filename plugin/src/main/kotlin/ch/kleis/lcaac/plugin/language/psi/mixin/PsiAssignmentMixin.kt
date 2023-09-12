package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.psi.LcaAssignment
import ch.kleis.lcaac.plugin.psi.LcaDataExpression
import ch.kleis.lcaac.plugin.psi.LcaDataRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

abstract class PsiAssignmentMixin(node: ASTNode) : ASTWrapperPsiElement(node), LcaAssignment {
    override fun getDataRef(): LcaDataRef {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, LcaDataRef::class.java).elementAt(0)
    }

    override fun getValue(): LcaDataExpression {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, LcaDataExpression::class.java).elementAt(1)
    }

    override fun getName(): String {
        return getDataRef().name
    }

    override fun setName(name: String): PsiElement {
        getDataRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getDataRef().nameIdentifier
    }
}
