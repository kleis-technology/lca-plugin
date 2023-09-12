package ch.kleis.lcaac.plugin.language.psi.mixin.spec

import ch.kleis.lcaac.plugin.language.psi.reference.ProcessReferenceFromPsiProcessTemplateSpec
import ch.kleis.lcaac.plugin.psi.LcaProcessTemplateSpec
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

abstract class PsiProcessTemplateSpecMixin(node: ASTNode) : ASTWrapperPsiElement(node), LcaProcessTemplateSpec {
    override fun getReference(): ProcessReferenceFromPsiProcessTemplateSpec {
        return ProcessReferenceFromPsiProcessTemplateSpec(this)
    }

    override fun getName(): String {
        return getProcessRef().name
    }

    override fun setName(name: String): PsiElement {
        getProcessRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getProcessRef().nameIdentifier
    }
}
