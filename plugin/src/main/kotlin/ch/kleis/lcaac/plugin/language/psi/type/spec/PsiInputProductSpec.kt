package ch.kleis.lcaac.plugin.language.psi.type.spec

import com.intellij.psi.PsiNameIdentifierOwner

interface PsiInputProductSpec : PsiNameIdentifierOwner {
    override fun getName(): String
}
