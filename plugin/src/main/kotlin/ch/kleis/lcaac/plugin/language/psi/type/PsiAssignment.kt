package ch.kleis.lcaac.plugin.language.psi.type

import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiDataRef
import ch.kleis.lcaac.plugin.psi.LcaDataExpression
import com.intellij.psi.PsiNameIdentifierOwner

interface PsiAssignment : PsiNameIdentifierOwner {
    override fun getName(): String

    fun getDataRef(): PsiDataRef

    fun getValue(): LcaDataExpression
}
