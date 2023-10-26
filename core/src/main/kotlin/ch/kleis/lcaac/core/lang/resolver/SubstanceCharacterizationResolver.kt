package ch.kleis.lcaac.core.lang.resolver

import ch.kleis.lcaac.core.lang.expression.EPackage
import ch.kleis.lcaac.core.lang.expression.ESubstanceCharacterization
import ch.kleis.lcaac.core.lang.expression.ESubstanceSpec

class SubstanceCharacterizationResolver<Q>(
    private val rootPkg: EPackage<Q>,
    private val pkgResolver: PkgResolver<Q>
) {
    // TODO: Test the root pkg  vs pkg resolve logic
    fun resolve(spec: ESubstanceSpec<Q>): ESubstanceCharacterization<Q>? {
        val pkg = if (spec.pkg == null) rootPkg else pkgResolver.resolve(spec)
        val name = spec.name
        val type = spec.type ?: return null
        val compartment = spec.compartment ?: return null

        return spec.subCompartment?.let { subCompartment ->
            pkg.getSubstanceCharacterization(name, type, compartment, subCompartment)
        } ?: pkg.getSubstanceCharacterization(name, type, compartment)
    }
}
