package ch.kleis.lcaac.plugin.language.psi.type

import com.intellij.psi.PsiElement

interface PsiExecute : PsiElement {

    fun getCommands(): List<String>
}
