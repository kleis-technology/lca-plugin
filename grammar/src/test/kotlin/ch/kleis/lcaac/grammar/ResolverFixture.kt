package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.resolver.PkgResolver
import ch.kleis.lcaac.core.lang.resolver.Resolver

class ResolverFixture {
    companion object {
        fun <Q> alwaysResolveTo(rootPkg: EPackage<Q>): Resolver<Q> = Resolver(
            rootPkg,
            object : PkgResolver<Q> {
                override fun resolve(spec: EProductSpec<Q>): EPackage<Q> = rootPkg
                override fun resolve(spec: ESubstanceSpec<Q>): EPackage<Q> = rootPkg
                override fun resolve(dataRef: EDataRef<Q>): EPackage<Q> = rootPkg
                override fun resolve(pkg: PackageExpression<Q>): EPackage<Q> = rootPkg
            }
        )
    }
}
