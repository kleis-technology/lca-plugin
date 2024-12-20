package ch.kleis.lcaac.plugin.language.psi.reference

import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.language.psi.stub.LcaStubIndexKeys
import ch.kleis.lcaac.plugin.language.psi.stub.global_assignment.GlobalAssigmentStubKeyIndex
import ch.kleis.lcaac.plugin.language.psi.stub.unit.UnitStubKeyIndex
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiDataRef
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.*
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.PsiTreeUtil

class DataReference(
    element: PsiDataRef
) : PsiPolyVariantReferenceBase<PsiDataRef>(element) {
    private val globalAssignmentRef = GlobalUIDOwnerReference(
        element,
        { project, fqn ->
            GlobalAssigmentStubKeyIndex.findGlobalAssignments(project, fqn)
        },
        { project ->
            StubIndex.getInstance().getAllKeys(LcaStubIndexKeys.GLOBAL_ASSIGNMENTS, project)
        }
    )
    private val unitRef = GlobalUIDOwnerReference(
        element,
        { project, fqn ->
            UnitStubKeyIndex.findUnits(project, fqn)
        },
        { project ->
            StubIndex.getInstance().getAllKeys(LcaStubIndexKeys.UNITS, project)
        }
    )

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val localMatches = resolveElementLocally(
            DataRefExactNameMatcherScopeProcessor(element)
        )
        if (localMatches.isNotEmpty()) {
            return localMatches.map { PsiElementResolveResult(it) }.toTypedArray()
        }
        return resolveElementInGlobalAssignments()
            .plus(resolveElementInUnitDefinitions())
            .map { PsiElementResolveResult(it) }
            .toTypedArray()
    }

    override fun getVariants(): Array<Any> {
        val localDefns: List<Any> = resolveElementLocally(
            DataRefCollectorScopeProcessor()
        )
            .mapNotNull { psi ->
                psi.name?.let {
                    LookupElementBuilder.create(it)
                }
            }
        val globalDefns = globalAssignmentRef.variants.toList()
        val unitDefns = unitRef.variants.toList()
        return localDefns.plus(globalDefns).plus(unitDefns).toTypedArray()
    }

    private fun resolveElementLocally(
        resolver: DataRefScopeProcessor
    ): Set<PsiNameIdentifierOwner> {
        var lastParent: PsiElement? = null
        val parents = PsiTreeUtil.collectParents(element, PsiElement::class.java, false) {
            it is LcaFile
        }
        for (parent in parents) {
            val doBreak = !parent.processDeclarations(resolver, ResolveState.initial(), lastParent, element)
            if (doBreak) {
                break
            }
            lastParent = parent
        }
        return resolver.getResults()
    }

    private fun resolveElementInGlobalAssignments(): Set<PsiElement> {
        return globalAssignmentRef.multiResolve(false)
            .mapNotNull { it.element }
            .toSet()
    }

    private fun resolveElementInUnitDefinitions(): Set<PsiElement> {
        return unitRef.multiResolve(false)
            .mapNotNull { it.element }
            .toSet()
    }
}
