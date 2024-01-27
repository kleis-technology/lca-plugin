package ch.kleis.lcaac.plugin.language.psi.type

import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiColumnRef
import ch.kleis.lcaac.plugin.psi.LcaDataExpression
import com.intellij.psi.PsiNameIdentifierOwner

interface PsiColumnDefinition : PsiNameIdentifierOwner {
    override fun getName(): String
    fun getColumnRef(): PsiColumnRef
    fun getValue(): LcaDataExpression
}
