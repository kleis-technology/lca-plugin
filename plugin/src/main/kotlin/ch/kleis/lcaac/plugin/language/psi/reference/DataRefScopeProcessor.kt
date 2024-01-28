package ch.kleis.lcaac.plugin.language.psi.reference

import ch.kleis.lcaac.plugin.language.psi.type.PsiBlockForEach
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiDataRef
import ch.kleis.lcaac.plugin.psi.LcaLabels
import ch.kleis.lcaac.plugin.psi.LcaParams
import ch.kleis.lcaac.plugin.psi.LcaVariables
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
        if (element is LcaVariables) {
            results.addAll(element.assignmentList)
        }

        if (element is LcaParams) {
            results.addAll(element.assignmentList)
        }

        if (element is LcaLabels) {
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
        if (element is LcaVariables) {
            return checkDecl(element.assignmentList)
        }

        if (element is LcaParams) {
            return checkDecl(element.assignmentList)
        }

        if (element is LcaLabels) {
            return checkDecl(element.labelAssignmentList)
        }

        if (element is PsiBlockForEach) {
            return checkDecl(listOf(element))
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
