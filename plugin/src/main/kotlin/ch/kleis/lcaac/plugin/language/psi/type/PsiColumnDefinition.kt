package ch.kleis.lcaac.plugin.language.psi.type

import com.intellij.psi.PsiElement

interface PsiColumnDefinition : PsiElement {
    fun getColumnName(): String
}
