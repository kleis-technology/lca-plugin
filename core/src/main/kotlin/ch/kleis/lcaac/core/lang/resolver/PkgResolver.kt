package ch.kleis.lcaac.core.lang.resolver

import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.lang.expression.EPackage
import ch.kleis.lcaac.core.lang.expression.EProductSpec
import ch.kleis.lcaac.core.lang.expression.ESubstanceSpec

interface PkgResolver<Q> {
    fun resolve(spec: EProductSpec<Q>): EPackage<Q>
    fun resolve(spec: ESubstanceSpec<Q>): EPackage<Q>
    fun resolve(dataRef: EDataRef<Q>): EPackage<Q>
}
