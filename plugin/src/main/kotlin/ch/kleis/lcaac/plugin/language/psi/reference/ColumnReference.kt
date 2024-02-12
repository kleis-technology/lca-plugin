package ch.kleis.lcaac.plugin.language.psi.reference

import ch.kleis.lcaac.plugin.language.psi.type.PsiBlockForEach
import ch.kleis.lcaac.plugin.language.psi.type.ref.PsiColumnRef
import ch.kleis.lcaac.plugin.psi.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.PsiTreeUtil

// TODO: Test me
class ColumnReference(
    element: PsiColumnRef
) : PsiPolyVariantReferenceBase<PsiColumnRef>(element) {
    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val localMatches = resolveColumnsMatching { it == element.name }
        return localMatches.map { PsiElementResolveResult(it) }.toTypedArray()
    }

    override fun getVariants(): Array<Any> {
        val localDefns = resolveColumnsMatching { true }
            .mapNotNull { psi ->
                psi.name?.let {
                    LookupElementBuilder.create(it)
                }
            }
        return localDefns.toTypedArray()
    }

    private fun resolveColumnsMatching(
        predicate: (String) -> Boolean
    ): Set<PsiNameIdentifierOwner> {
        return when (val parent = element.parent) {
            is LcaSliceExpression -> {
                resolveUntilRecordExpression(parent.dataRef)
                    ?.let { checkDataSourceExpression(it, predicate) }
                    ?: emptySet()
            }

            is LcaColExpression -> {
                parent.dataSourceExpression?.let {
                    checkDataSourceExpression(it, predicate)
                } ?: emptySet()
            }
            is LcaRowSelector -> {
                val dataSourceExpression = PsiTreeUtil.getParentOfType(parent, LcaDataSourceExpression::class.java)
                    ?: return emptySet()
                checkDataSourceExpression(dataSourceExpression, predicate)
            }

            else -> emptySet()
        }
    }

    private fun resolveUntilRecordExpression(ref: LcaDataRef): LcaDataSourceExpression? {
        val target = ref.reference.resolve() ?: return null
        return when (target) {
            is LcaAssignment -> {
                when (val value = target.getValue()) {
                    is LcaRecordExpression -> value.dataSourceExpression
                    is LcaDataRef -> resolveUntilRecordExpression(value)
                    else -> null
                }
            }

            is LcaGlobalAssignment -> {
                when (val value = target.getValue()) {
                    is LcaRecordExpression -> value.dataSourceExpression
                    is LcaDataRef -> resolveUntilRecordExpression(value)
                    else -> null
                }
            }

            is PsiBlockForEach -> target.getValue()
            else -> null
        }
    }

    private fun checkDataSourceExpression(expression: LcaDataSourceExpression, predicate: (String) -> Boolean): Set<PsiNameIdentifierOwner> {
        val ds = expression
            .dataSourceRef.reference.resolve() as LcaDataSourceDefinition?
            ?: return emptySet()
        val schema = ds.schemaDefinitionList
            .firstOrNull() ?: return emptySet()
        val columns = schema
            .columnDefinitionList
            .filter { predicate(it.name) }
        return columns.toSet()
    }
}
