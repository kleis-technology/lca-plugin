package ch.kleis.lcaac.plugin.language.psi.type

import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiProcessRef
import ch.kleis.lcaac.plugin.language.psi.type.spec.PsiProcessTemplateSpec
import com.intellij.psi.PsiNamedElement

interface PsiGenerate : PsiNamedElement {

    fun getProcessTemplateSpecRef(): PsiProcessTemplateSpec
    fun getProcessRef(): PsiProcessRef
}
