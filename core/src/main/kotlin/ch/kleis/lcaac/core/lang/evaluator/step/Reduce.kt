package ch.kleis.lcaac.core.lang.evaluator.step

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.Helper
import ch.kleis.lcaac.core.lang.evaluator.reducer.LcaExpressionReducer
import ch.kleis.lcaac.core.lang.evaluator.reducer.TemplateExpressionReducer
import ch.kleis.lcaac.core.lang.expression.EProcess
import ch.kleis.lcaac.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaac.core.lang.expression.ESubstanceCharacterization
import ch.kleis.lcaac.core.lang.resolver.Resolver
import ch.kleis.lcaac.core.math.QuantityOperations

class Reduce<Q>(
    private val resolver: Resolver<Q>,
    ops: QuantityOperations<Q>,
) {
    private val lcaReducer = LcaExpressionReducer(resolver, ops)
    private val templateReducer = TemplateExpressionReducer(resolver, ops)

    fun apply(expression: EProcessTemplateApplication<Q>): EProcess<Q> {
        val reduced = templateReducer.reduce(expression)
        val unboundedReferences = Helper<Q>().allRequiredRefs(reduced)
        if (unboundedReferences.isNotEmpty()) {
            throw EvaluatorException("unbounded references: $unboundedReferences")
        }
        return reduced
    }

    fun apply(expression: ESubstanceCharacterization<Q>): ESubstanceCharacterization<Q> {
        val reduced = lcaReducer.reduceSubstanceCharacterization(expression)
        val unboundedReferences = Helper<Q>().allRequiredRefs(reduced)
        if (unboundedReferences.isNotEmpty()) {
            throw EvaluatorException("unbounded references: $unboundedReferences")
        }
        return reduced
    }


}
