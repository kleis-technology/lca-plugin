package ch.kleis.lcaac.core.lang.resolver

import ch.kleis.lcaac.core.lang.expression.EImport
import ch.kleis.lcaac.core.lang.expression.EPackage
import ch.kleis.lcaac.core.lang.expression.SubstanceType

interface ImportResolver<Q> {
    fun resolve(import: EImport<Q>, hint: ImportHint? = null): EPackage<Q>
}

/*
    Hint allows further optimizations.
    E.g., when importing a substance from EF 3.1,
    no need to include all the substances in the retrieved package.
 */

sealed interface ImportHint

data class HintData(
    val name: String
) : ImportHint

data class HintProcess(
    val name: String,
    val labels: Map<String, String> = emptyMap(),
) : ImportHint

data class HintSubstance(
    val name: String,
    val type: SubstanceType,
    val compartment: String,
    val subCompartment: String? = null,
) : ImportHint
