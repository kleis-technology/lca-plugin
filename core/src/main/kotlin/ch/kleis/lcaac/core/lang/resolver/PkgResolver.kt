package ch.kleis.lcaac.core.lang.resolver

import ch.kleis.lcaac.core.lang.expression.*

interface PkgResolver<Q> {
    fun resolve(spec: EProductSpec<Q>): EPackage<Q>
    fun resolve(spec: ESubstanceSpec<Q>): EPackage<Q>
    fun resolve(dataRef: EDataRef<Q>): EPackage<Q>
    fun resolve(pkg: PackageExpression<Q>): EPackage<Q>
}
