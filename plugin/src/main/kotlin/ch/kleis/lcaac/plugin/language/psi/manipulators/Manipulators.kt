package ch.kleis.lcaac.plugin.language.psi.manipulators

import ch.kleis.lcaac.plugin.language.psi.type.PsiAssignment
import ch.kleis.lcaac.plugin.language.psi.type.PsiGlobalAssignment
import ch.kleis.lcaac.plugin.language.psi.type.PsiLabelAssignment
import ch.kleis.lcaac.plugin.language.psi.type.ref.*
import ch.kleis.lcaac.plugin.language.psi.type.spec.PsiInputProductSpec
import ch.kleis.lcaac.plugin.language.psi.type.spec.PsiOutputProductSpec
import ch.kleis.lcaac.plugin.language.psi.type.spec.PsiProcessTemplateSpec
import ch.kleis.lcaac.plugin.language.psi.type.spec.PsiSubstanceSpec
import ch.kleis.lcaac.plugin.language.psi.type.trait.PsiUIDOwner
import ch.kleis.lcaac.plugin.psi.*
import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.PsiElement

sealed class PsiUIDOwnerManipulator<E : PsiUIDOwner> : AbstractElementManipulator<E>() {
    override fun handleContentChange(element: E, range: TextRange, newContent: String?): E {
        newContent?.let { element.setName(it) }
        return element
    }
}

class PsiQuantityRefManipulator : PsiUIDOwnerManipulator<PsiDataRef>()
class PsiSubstanceRefManipulator : PsiUIDOwnerManipulator<PsiSubstanceRef>()
class PsiProcessTemplateRefManipulator : PsiUIDOwnerManipulator<PsiProcessRef>()
class PsiLabelRefManipulator : PsiUIDOwnerManipulator<PsiLabelRef>()
class PsiParameterRefManipulator : PsiUIDOwnerManipulator<PsiParameterRef>()
class PsiProductRefManipulator : PsiUIDOwnerManipulator<PsiProductRef>()

sealed class PsiDelegateManipulator<E : PsiElement>(
    private val getter: (E) -> PsiUIDOwner
) : AbstractElementManipulator<E>() {
    override fun handleContentChange(element: E, range: TextRange, newContent: String?): E? {
        newContent?.let { getter(element).setName(it) }
        return element
    }
}

class PsiSubstanceSpecManipulator : PsiDelegateManipulator<LcaSubstanceSpec>(
    { it.substanceRef }
)

class PsiInputProductSpecManipulator : PsiDelegateManipulator<LcaInputProductSpec>(
    { it.getProductRef() }
)

class PsiOutputProductSpecManipulator : PsiDelegateManipulator<LcaOutputProductSpec>(
    { it.productRef }
)

class PsiProcessTemplateSpecManipulator : PsiDelegateManipulator<LcaProcessTemplateSpec>(
    { it.processRef }
)

class PsiLabelAssignmentManipulator : PsiDelegateManipulator<PsiLabelAssignment>(
    { it.getLabelRef() }
)

class PsiGlobalAssignmentManipulator : PsiDelegateManipulator<PsiGlobalAssignment>(
    { it.getDataRef() }
)

class PsiAssignmentManipulator : PsiDelegateManipulator<PsiAssignment>(
    { it.getDataRef() }
)
