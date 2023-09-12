package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.psi.LcaLabelAssignment
import ch.kleis.lcaac.plugin.psi.LcaLabelRef
import ch.kleis.lcaac.plugin.psi.LcaTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

abstract class PsiLabelAssignmentMixin(node: ASTNode) : ASTWrapperPsiElement(node), LcaLabelAssignment {
    override fun getLabelRef(): LcaLabelRef {
        return PsiTreeUtil.getChildOfType(this, LcaLabelRef::class.java) as LcaLabelRef
    }

    override fun getValue(): String {
        return node.findChildByType(LcaTypes.STRING_LITERAL)?.text?.trim('"') as String
    }

    override fun getName(): String {
        return getLabelRef().name
    }

    override fun setName(name: String): PsiElement {
        getLabelRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getLabelRef().nameIdentifier
    }
}
