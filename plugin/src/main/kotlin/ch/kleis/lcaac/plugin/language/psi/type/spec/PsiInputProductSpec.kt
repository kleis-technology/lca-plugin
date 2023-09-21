package ch.kleis.lcaac.plugin.language.psi.type.spec

import ch.kleis.lcaac.plugin.language.psi.reference.OutputProductReferenceFromPsiInputProductSpec
import ch.kleis.lcaac.plugin.psi.LcaProcessTemplateSpec
import ch.kleis.lcaac.plugin.psi.LcaProductRef
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

interface PsiInputProductSpec : PsiNameIdentifierOwner {
    override fun getName(): String
}
