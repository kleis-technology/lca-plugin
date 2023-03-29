package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.type.PsiFromProcessConstraint
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiArgument
import ch.kleis.lcaplugin.language.psi.type.ref.PsiParameterRef
import com.intellij.psi.*

class ParameterReference(
    element: PsiParameterRef
) : PsiReferenceBase<PsiParameterRef>(element), PsiPolyVariantReference {
    override fun resolve(): PsiElement? {
        val results = multiResolve(false)
        return if (results.size == 1) results.first().element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val argument = element.parent
        if (argument !is PsiArgument) {
            return emptyArray()
        }
        val fromProcessConstraint = argument.parent
        if (fromProcessConstraint !is PsiFromProcessConstraint) {
            return emptyArray()
        }
        return fromProcessConstraint.getProcessTemplateRef().reference.multiResolve(false)
            .mapNotNull { it.element }
            .filterIsInstance<PsiProcess>()
            .flatMap { process ->
                process.getPsiParametersBlocks()
                    .flatMap { it.getAssignments() }
                    .filter { it.getQuantityRef().name == element.name }
            }
            .map { PsiElementResolveResult(it) }
            .toTypedArray()
    }
}
