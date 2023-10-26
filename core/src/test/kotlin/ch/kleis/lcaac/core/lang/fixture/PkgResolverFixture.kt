package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.expression.EDataRef
import ch.kleis.lcaac.core.lang.expression.EPackage
import ch.kleis.lcaac.core.lang.expression.EProductSpec
import ch.kleis.lcaac.core.lang.expression.ESubstanceSpec
import ch.kleis.lcaac.core.lang.resolver.PkgResolver

class PkgResolverFixture {
    companion object {
        fun <Q> alwaysResolveTo(pkg: EPackage<Q>): PkgResolver<Q> {
            return object : PkgResolver<Q> {
                override fun resolve(spec: EProductSpec<Q>): EPackage<Q> {
                    return pkg
                }

                override fun resolve(spec: ESubstanceSpec<Q>): EPackage<Q> {
                    return pkg
                }

                override fun resolve(dataRef: EDataRef<Q>): EPackage<Q> {
                    return pkg
                }
            }
        }
    }
}
