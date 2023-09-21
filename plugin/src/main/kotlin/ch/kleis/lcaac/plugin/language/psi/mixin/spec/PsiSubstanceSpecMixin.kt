package ch.kleis.lcaac.plugin.language.psi.mixin.spec

import ch.kleis.lcaac.core.lang.expression.SubstanceType
import ch.kleis.lcaac.plugin.language.psi.reference.SubstanceReferenceFromPsiSubstanceSpec
import ch.kleis.lcaac.plugin.psi.LcaBlockEmissions
import ch.kleis.lcaac.plugin.psi.LcaBlockLandUse
import ch.kleis.lcaac.plugin.psi.LcaBlockResources
import ch.kleis.lcaac.plugin.psi.LcaSubstanceSpec
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

abstract class PsiSubstanceSpecMixin(node: ASTNode) : ASTWrapperPsiElement(node), LcaSubstanceSpec {
    override fun getReference(): SubstanceReferenceFromPsiSubstanceSpec {
        return SubstanceReferenceFromPsiSubstanceSpec(this)
    }

    override fun getType(): SubstanceType? {
        return PsiTreeUtil.findFirstParent(this) { p -> p is LcaBlockEmissions }?.let { SubstanceType.EMISSION }
            ?: PsiTreeUtil.findFirstParent(this) { p -> p is LcaBlockResources }?.let { SubstanceType.RESOURCE }
            ?: PsiTreeUtil.findFirstParent(this) { p -> p is LcaBlockLandUse }?.let { SubstanceType.LAND_USE }
    }

    override fun getName(): String {
        return getSubstanceRef().name
    }

    override fun setName(name: String): PsiElement {
        getSubstanceRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getSubstanceRef().nameIdentifier
    }
}
