package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.resolver.PkgResolver
import ch.kleis.lcaac.core.math.QuantityOperations

class CorePkgResolver<Q>(
    sourceSet: SourceSet,
    ops: QuantityOperations<Q>,
    loader: PkgLoader<Q> = PkgLoader(sourceSet, ops),
) : PkgResolver<Q> {
    private val pkgs = sourceSet.pkgNames()
        .associateWith { loader.load(it) }

    override fun resolve(spec: EProductSpec<Q>): EPackage<Q> {
        val from = spec.from ?: throw EvaluatorException("cannot resolve package from $spec")
        return when (from) {
            is FromPackage -> when (val pkg = from.pkg) {
                is EImport -> pkgs[pkg.name] ?: throw EvaluatorException("unknown package ${pkg.name}")
                is EImportRef -> TODO()
            }

            is FromProcess -> when (val pkg = from.pkg) {
                is EImport -> pkgs[pkg.name] ?: throw EvaluatorException("unknown package ${pkg.name}")
                is EImportRef -> TODO()
                null -> throw EvaluatorException("cannot resolve package from $spec")
            }
        }
    }

    override fun resolve(spec: ESubstanceSpec<Q>): EPackage<Q> {
        val from = spec.from ?: throw EvaluatorException("cannot resolve package from $spec")
        return when(from) {
            is EImport -> pkgs[from.name] ?: throw EvaluatorException("unknown package ${from.name}")
            is EImportRef -> TODO()
        }
    }

    override fun resolve(dataRef: EDataRef<Q>): EPackage<Q> {
        val from = dataRef.from ?: throw EvaluatorException("cannot resolve package from $dataRef")
        return when(from) {
            is EImport -> pkgs[from.name] ?: throw EvaluatorException("unknown package ${from.name}")
            is EImportRef -> TODO()
        }
    }

    override fun resolve(pkg: PackageExpression<Q>): EPackage<Q> {
        return when(pkg) {
            is EPackage -> pkg
            is EImport -> pkgs[pkg.name] ?: throw EvaluatorException("unknown package ${pkg.name}")
            is EImportRef -> TODO()
        }
    }
}
