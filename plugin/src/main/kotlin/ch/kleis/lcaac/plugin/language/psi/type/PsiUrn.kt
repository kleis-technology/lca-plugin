package ch.kleis.lcaac.plugin.language.psi.type

import com.intellij.psi.PsiElement

interface PsiUrn : PsiElement {
    fun getParts(): List<String>
}
