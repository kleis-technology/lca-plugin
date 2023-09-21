package ch.kleis.lcaac.plugin.language.psi.type

import ch.kleis.lcaac.plugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiMetaAssignment : PsiElement {
    fun getName(): String?

    fun getKey(): String

    fun getValue(): String
}
