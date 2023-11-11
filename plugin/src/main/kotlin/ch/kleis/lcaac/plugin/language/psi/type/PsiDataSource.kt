package ch.kleis.lcaac.plugin.language.psi.type

import ch.kleis.lcaac.core.lang.register.DataSourceKey
import com.intellij.psi.PsiNameIdentifierOwner

interface PsiDataSource : PsiNameIdentifierOwner {
    fun buildUniqueKey(): DataSourceKey
}
