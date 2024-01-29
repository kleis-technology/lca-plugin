package ch.kleis.lcaac.plugin.language.psi.type

import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiDataRef
import ch.kleis.lcaac.plugin.psi.LcaDataSourceExpression
import ch.kleis.lcaac.plugin.psi.LcaVariables
import com.intellij.psi.PsiNameIdentifierOwner

interface PsiBlockForEach : PsiNameIdentifierOwner {
    override fun getName(): String

    fun getDataRef(): PsiDataRef

    fun getValue(): LcaDataSourceExpression?

    fun getVariablesList(): Collection<LcaVariables>
}
