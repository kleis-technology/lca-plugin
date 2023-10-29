package ch.kleis.lcaac.core.lang.resolver

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.register.ImportKey

class Resolver<Q>(
    val rootPkg: EPackage<Q>,
    private val importResolver: ImportResolver<Q>,
) {
    fun withRoot(pkg: EPackage<Q>): Resolver<Q> = Resolver(pkg, importResolver)

    private fun fetchPkg(e: PackageImportExpression<Q>?, hint: ImportHint? = null): EPackage<Q> {
        return when (e) {
            null -> rootPkg
            is EImportRef -> {
                val e2 = rootPkg.imports[ImportKey(e.name)] ?: return rootPkg
                importResolver.resolve(e2, hint)
            }

            is EImport -> importResolver.resolve(e, hint)
        }
    }

    private fun fetchPkg(e: FromExpression<Q>?, hint: ImportHint? = null): EPackage<Q> {
        return when (e) {
            is FromPackage -> fetchPkg(e.pkg, hint)
            is FromProcess -> fetchPkg(e.pkg, hint)
            null -> rootPkg
        }
    }

    fun resolve(dataRef: EDataRef<Q>): DataExpression<Q>? {
        val pkg = fetchPkg(dataRef.from, HintData(dataRef.name))
        return pkg.getData(dataRef.name)
    }

    fun resolve(spec: EProductSpec<Q>): EProcessTemplate<Q>? {
        if (spec.from !is FromProcess) {
            val pkg = fetchPkg(spec.from)
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
        val pkg = fetchPkg(
            spec.from,
            HintProcess(
                name,
                labels,
            )
        )
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
        val name = spec.name
        val type = spec.type ?: return null
        val compartment = spec.compartment ?: return null
        val subCompartment = spec.subCompartment
        val pkg = fetchPkg(
            spec.from,
            HintSubstance(
                name, type, compartment, subCompartment
            ),
        )
        return pkg.getSubstanceCharacterization(name, type, compartment, subCompartment)
            ?: pkg.getSubstanceCharacterization(name, type, compartment)
    }
}
