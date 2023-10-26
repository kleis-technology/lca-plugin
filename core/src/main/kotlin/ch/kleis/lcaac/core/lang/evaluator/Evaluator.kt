package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.lang.evaluator.protocol.Learner
import ch.kleis.lcaac.core.lang.evaluator.protocol.Oracle
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.resolver.PkgResolver
import ch.kleis.lcaac.core.math.QuantityOperations
import org.slf4j.LoggerFactory

class Evaluator<Q>(
    private val pkg: EPackage<Q>,
    private val pkgResolver: PkgResolver<Q>,
    private val ops: QuantityOperations<Q>,
) {
    @Suppress("PrivatePropertyName")
    private val LOG = LoggerFactory.getLogger(Evaluator::class.java)

    fun trace(initialRequests: Set<EProductSpec<Q>>): EvaluationTrace<Q> {
        val learner = Learner(initialRequests, ops)
        val oracle = Oracle(pkg, pkgResolver, ops)
        LOG.info("Start evaluation")
        try {
            var requests = learner.start()
            while (requests.isNotEmpty()) {
                val responses = oracle.answer(requests)
                requests = learner.receive(responses)
            }
            LOG.info("End evaluation, found ${learner.trace.getNumberOfProcesses()} processes and ${learner.trace.getNumberOfSubstanceCharacterizations()} substances")
            return learner.trace
        } catch (e: Exception) {
            LOG.info("End evaluation with error $e")
            throw e
        }
    }

    fun trace(
        templateName: String,
        arguments: Map<String, DataExpression<Q>> = emptyMap(),
        labels: Map<String, String> = emptyMap(),
    ): EvaluationTrace<Q> {
        val template = pkg.getTemplate(templateName, labels)
            ?: throw EvaluatorException("unknown process template $templateName$labels")
        return prepareRequests(template, arguments)
            .let(this::trace)
    }

    private fun prepareRequests(
        template: EProcessTemplate<Q>,
        arguments: Map<String, DataExpression<Q>> = emptyMap(),
    ): Set<EProductSpec<Q>> {
        val body = template.body
        return body.products.map {
            it.product.copy(
                fromProcess = FromProcess(
                    body.name,
                    MatchLabels(body.labels),
                    template.params.plus(arguments),
                    pkg = pkg,
                )
            )
        }.toSet()
    }
}

