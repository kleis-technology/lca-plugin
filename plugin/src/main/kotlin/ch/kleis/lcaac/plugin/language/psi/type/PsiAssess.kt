package ch.kleis.lcaac.plugin.language.psi.type

import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiProcessRef
import com.intellij.psi.PsiNamedElement

interface PsiAssess : PsiNamedElement {

    fun getProcessRef(): PsiProcessRef
}
