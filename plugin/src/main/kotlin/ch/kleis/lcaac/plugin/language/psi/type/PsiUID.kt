package ch.kleis.lcaac.plugin.language.psi.type

import ch.kleis.lcaac.plugin.language.psi.factory.LcaFileFactory
import ch.kleis.lcaac.plugin.language.psi.factory.LcaUIDFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

interface PsiUID : PsiNamedElement {
    override fun getName(): String
}
