package ch.kleis.lcaac.plugin.language.psi.type

import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiLabelRef
import ch.kleis.lcaac.plugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

interface PsiLabelAssignment : PsiNameIdentifierOwner {
    fun getLabelRef(): PsiLabelRef

    fun getValue(): String

    override fun getName(): String
}
