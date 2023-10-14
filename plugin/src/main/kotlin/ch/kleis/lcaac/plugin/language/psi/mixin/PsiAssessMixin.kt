package ch.kleis.lcaac.plugin.language.psi.mixin

import ch.kleis.lcaac.plugin.language.psi.type.PsiAssess
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiProcessRef
import ch.kleis.lcaac.plugin.psi.LcaProcessRef
import ch.kleis.lcaac.plugin.psi.LcaProcessTemplateSpec
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

abstract class PsiAssessMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiAssess {
    override fun getName(): String {
        return this.getProcessRef().name
    }

    override fun setName(name: String): PsiElement {
        getProcessRef().name = name
        return this
    }

    override fun getProcessRef(): PsiProcessRef {
        return PsiTreeUtil.getChildrenOfTypeAsList(getProcessTemplateSpecRef(), LcaProcessRef::class.java).elementAt(0)
    }

    override fun getProcessTemplateSpecRef(): LcaProcessTemplateSpec {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, LcaProcessTemplateSpec::class.java).first()
    }

    override fun toString(): String {
        return "uid(${this.name})"
    }
}
