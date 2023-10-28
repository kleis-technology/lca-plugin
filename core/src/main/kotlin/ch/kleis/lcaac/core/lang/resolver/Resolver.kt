package ch.kleis.lcaac.core.lang.resolver

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.*

class Resolver<Q>(
    val rootPkg: EPackage<Q>,
    private val pkgResolver: PkgResolver<Q>,
) {
    fun withRoot(pkg: EPackage<Q>): Resolver<Q> = Resolver(pkg, pkgResolver)

    fun resolve(dataRef: EDataRef<Q>): DataExpression<Q>? {
        val pkg = if (dataRef.from == null) rootPkg else pkgResolver.resolve(dataRef)
        return pkg.getData(dataRef.name)
    }

    fun resolve(spec: EProductSpec<Q>): EProcessTemplate<Q>? {
        val pkg = when(spec.from) {
            null -> rootPkg
            else -> pkgResolver.resolve(spec)
        }
        if (spec.from !is FromProcess) {
            val matches = pkg.getAllTemplatesByProductName(spec.name)
            return when (matches.size) {
                0 -> null
                1 -> matches.first()
                else -> throw EvaluatorException("more than one processes found providing ${spec.name}")
            }
        }

        val name = spec.from.name
        val labels = spec.from.matchLabels.elements.mapValues {
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

    fun resolve(spec: ESubstanceSpec<Q>): ESubstanceCharacterization<Q>? {
        val pkg = if (spec.from == null) rootPkg else pkgResolver.resolve(spec)
        val name = spec.name
        val type = spec.type ?: return null
        val compartment = spec.compartment ?: return null

        return spec.subCompartment?.let { subCompartment ->
            pkg.getSubstanceCharacterization(name, type, compartment, subCompartment)
        } ?: pkg.getSubstanceCharacterization(name, type, compartment)
    }
}
