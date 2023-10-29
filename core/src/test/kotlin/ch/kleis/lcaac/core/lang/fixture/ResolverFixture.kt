package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.resolver.ImportHint
import ch.kleis.lcaac.core.lang.resolver.ImportResolver
import ch.kleis.lcaac.core.lang.resolver.Resolver

class ResolverFixture {
    companion object {
        fun <Q> alwaysResolveTo(rootPkg: EPackage<Q>): Resolver<Q> {
            return Resolver(
                rootPkg,
                object : ImportResolver<Q> {
                    override fun resolve(import: EImport<Q>, hint: ImportHint?): EPackage<Q> {
                        return rootPkg
                    }
                }
            )
        }
    }
}
