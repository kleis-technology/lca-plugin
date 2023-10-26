package ch.kleis.lcaac.core.lang.resolver

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.EPackage
import ch.kleis.lcaac.core.lang.expression.EProcessTemplate
import ch.kleis.lcaac.core.lang.expression.EProductSpec
import ch.kleis.lcaac.core.lang.expression.EStringLiteral

class ProcessResolver<Q>(
    private val rootPkg: EPackage<Q>,
    private val pkgResolver: PkgResolver<Q>
) {
    // TODO: Test the root pkg  vs pkg resolve logic
    fun resolve(spec: EProductSpec<Q>): EProcessTemplate<Q>? {
        val pkg = if (spec.fromProcess?.pkg == null) rootPkg else pkgResolver.resolve(spec)
        if (spec.fromProcess == null) {
            val matches = pkg.getAllTemplatesByProductName(spec.name)
            return when (matches.size) {
                0 -> null
                1 -> matches.first()
                else -> throw EvaluatorException("more than one processes found providing ${spec.name}")
            }
        }

        val name = spec.fromProcess.name
        val labels = spec.fromProcess.matchLabels.elements.mapValues {
                when (val v = it.value) {
                    is EStringLiteral -> v.value
                    else -> throw EvaluatorException("$v is not a valid label value")
                }
            }
        return pkg.getTemplate(name, labels)?.let { candidate ->
            val providedProducts = candidate.body.products.map { it.product.name }
            if (!providedProducts.contains(spec.name)) {
                val s = if (labels.isEmpty()) name else "$name$labels"
                throw EvaluatorException("no process '$s' providing '${spec.name}' found")
            }
            candidate
        }
    }
}
