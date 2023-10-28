package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.resolver.PkgResolver

class PkgResolverFixture {
    companion object {
        fun <Q> alwaysResolveTo(rootPkg: EPackage<Q>): PkgResolver<Q> {
            return object : PkgResolver<Q> {
                override fun resolve(spec: EProductSpec<Q>): EPackage<Q> {
                    return rootPkg
                }

                override fun resolve(spec: ESubstanceSpec<Q>): EPackage<Q> {
                    return rootPkg
                }

                override fun resolve(dataRef: EDataRef<Q>): EPackage<Q> {
                    return rootPkg
                }

                override fun resolve(pkg: PackageExpression<Q>): EPackage<Q> {
                    return rootPkg
                }
            }
        }
    }
}
