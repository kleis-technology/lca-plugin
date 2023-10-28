package ch.kleis.lcaac.core.lang.resolver

import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.lang.expression.EPackage

class DataResolver<Q>(
    private val rootPkg: EPackage<Q>,
    private val pkgResolver: PkgResolver<Q>
) {
    fun resolve(dataRef: EDataRef<Q>): DataExpression<Q>? {
        val pkg = if (dataRef.from == null) rootPkg else pkgResolver.resolve(dataRef)
        return pkg.getData(dataRef.name)
    }
}
