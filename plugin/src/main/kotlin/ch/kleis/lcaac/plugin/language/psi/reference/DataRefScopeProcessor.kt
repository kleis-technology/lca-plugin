package ch.kleis.lcaac.plugin.language.psi.reference

import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiDataRef
import ch.kleis.lcaac.plugin.psi.LcaBlockLabels
import ch.kleis.lcaac.plugin.psi.LcaBlockParameters
import ch.kleis.lcaac.plugin.psi.LcaBlockVariables
import ch.kleis.lcaac.plugin.psi.LcaGuardedAssignment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor

interface DataRefScopeProcessor : PsiScopeProcessor {
    fun getResults(): Set<PsiNameIdentifierOwner>
}

class DataRefCollectorScopeProcessor : DataRefScopeProcessor {
    private var results: MutableSet<PsiNameIdentifierOwner> = mutableSetOf()
    override fun execute(element: PsiElement, state: ResolveState): Boolean {
        if (element is LcaBlockVariables) {
            results.addAll(element.assignmentList)
        }

        if (element is LcaBlockParameters) {
            results.addAll(element.guardedAssignmentList.map(LcaGuardedAssignment::getAssignment))
        }

        if (element is LcaBlockLabels) {
            results.addAll(element.labelAssignmentList)
        }
        return true
    }

    override fun getResults(): Set<PsiNameIdentifierOwner> {
        return results
    }
}

class DataRefExactNameMatcherScopeProcessor(
    private val dataRef: PsiDataRef
) : DataRefScopeProcessor {
    private var results: Set<PsiNameIdentifierOwner> = emptySet()

    override fun execute(element: PsiElement, state: ResolveState): Boolean {
        if (element is LcaBlockVariables) {
            return checkDecl(element.assignmentList)
        }

        if (element is LcaBlockParameters) {
            return checkDecl(element.guardedAssignmentList.map(LcaGuardedAssignment::getAssignment))
        }

        if (element is LcaBlockLabels) {
            return checkDecl(element.labelAssignmentList)
        }

        return true
    }

    private fun checkDecl(entries: Collection<PsiNameIdentifierOwner>): Boolean {
        results = entries.filter { it.name == dataRef.name }.toSet()
        return results.isEmpty()
    }

    override fun getResults(): Set<PsiNameIdentifierOwner> {
        return results
    }
}
