package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.EImport
import ch.kleis.lcaac.core.lang.expression.EPackage
import ch.kleis.lcaac.core.lang.resolver.ImportHint
import ch.kleis.lcaac.core.lang.resolver.ImportResolver
import ch.kleis.lcaac.core.math.QuantityOperations

class CoreImportResolver<Q>(
    sourceSet: SourceSet,
    ops: QuantityOperations<Q>,
    loader: PkgLoader<Q> = PkgLoader(sourceSet, ops),
) : ImportResolver<Q> {
    private val pkgs = sourceSet.pkgNames()
        .associateWith { loader.load(it) }

    // We ignore hint for now
    override fun resolve(import: EImport<Q>, hint: ImportHint?): EPackage<Q> {
        return pkgs[import.name] ?: throw EvaluatorException("unknown package ${import.name}")
    }
}
