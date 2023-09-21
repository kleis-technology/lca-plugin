package ch.kleis.lcaac.plugin.language.psi.type.spec

import ch.kleis.lcaac.core.lang.expression.SubstanceType
import ch.kleis.lcaac.plugin.language.psi.reference.SubstanceReferenceFromPsiSubstanceSpec
import ch.kleis.lcaac.plugin.language.psi.type.field.PsiStringLiteralField
import ch.kleis.lcaac.plugin.psi.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

interface PsiSubstanceSpec : PsiNameIdentifierOwner {
    override fun getName(): String

    fun getType(): SubstanceType?
}
