package ch.kleis.lcaac.core.lang.evaluator.protocol

import ch.kleis.lcaac.core.lang.evaluator.step.CompleteTerminals
import ch.kleis.lcaac.core.lang.evaluator.step.Reduce
import ch.kleis.lcaac.core.lang.evaluator.step.ReduceLabelSelectors
import ch.kleis.lcaac.core.lang.expression.EPackage
import ch.kleis.lcaac.core.lang.expression.EProcess
import ch.kleis.lcaac.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaac.core.lang.expression.FromProcess
import ch.kleis.lcaac.core.lang.resolver.PkgResolver
import ch.kleis.lcaac.core.lang.resolver.ProcessResolver
import ch.kleis.lcaac.core.lang.resolver.SubstanceCharacterizationResolver
import ch.kleis.lcaac.core.math.QuantityOperations

class Oracle<Q>(
    pkg: EPackage<Q>,
    pkgResolver: PkgResolver<Q>,
    val ops: QuantityOperations<Q>,
) {
    private val reduceLabelSelectors = ReduceLabelSelectors(pkg, pkgResolver, ops)
    private val reduceDataExpressions = Reduce(pkg, pkgResolver, ops)
    private val completeTerminals = CompleteTerminals(ops)
    private val processResolver = ProcessResolver(pkg, pkgResolver)
    private val substanceCharacterizationResolver = SubstanceCharacterizationResolver(pkg, pkgResolver)

    fun answer(ports: Set<Request<Q>>): Set<Response<Q>> {
        return ports.mapNotNull { answerRequest(it) }.toSet()
    }

    private fun answerRequest(expression: Request<Q>): Response<Q>? {
        return when (expression) {
            is ProductRequest -> answerProductRequest(expression)
            is SubstanceRequest -> answerSubstanceRequest(expression)
        }
    }

    private fun answerProductRequest(request: ProductRequest<Q>): ProductResponse<Q>? {
        val spec = request.value
        val template = processResolver.resolve(spec) ?: return null
        val arguments = template.params
            .plus(if (spec.from is FromProcess<Q>) spec.from.arguments else emptyMap())
        val expression = EProcessTemplateApplication(template, arguments)
        val process = expression
            .let(reduceLabelSelectors::apply)
            .let(reduceDataExpressions::apply)
            .let(completeTerminals::apply)
        val selectedPortIndex = indexOf(request.value.name, process)
        return ProductResponse(request.address, process, selectedPortIndex)
    }

    private fun answerSubstanceRequest(request: SubstanceRequest<Q>): SubstanceResponse<Q>? {
        val spec = request.value
        return substanceCharacterizationResolver.resolve(spec)
            ?.takeIf { it.hasImpacts() }
            ?.let(reduceDataExpressions::apply)
            ?.let(completeTerminals::apply)
            ?.let { SubstanceResponse(request.address, it) }
    }

    private fun indexOf(productName: String, process: EProcess<Q>): Int {
        return process.products.indexOfFirst { it.product.name == productName }
    }

}
