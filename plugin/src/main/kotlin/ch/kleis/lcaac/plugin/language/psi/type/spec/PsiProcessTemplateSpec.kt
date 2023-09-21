package ch.kleis.lcaac.plugin.language.psi.type.spec

import ch.kleis.lcaac.plugin.language.psi.reference.ProcessReferenceFromPsiProcessTemplateSpec
import ch.kleis.lcaac.plugin.psi.LcaMatchLabels
import ch.kleis.lcaac.plugin.psi.LcaProcessRef
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

interface PsiProcessTemplateSpec : PsiNameIdentifierOwner {
    override fun getName(): String
}
