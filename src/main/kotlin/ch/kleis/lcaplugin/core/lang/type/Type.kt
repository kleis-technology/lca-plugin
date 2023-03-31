package ch.kleis.lcaplugin.core.lang.type

import ch.kleis.lcaplugin.core.lang.Dimension

sealed interface Type

data class TUnit(val dimension: Dimension) : Type

data class TQuantity(val dimension: Dimension) : Type

sealed interface TypeLcaExpression : Type

data class TProduct(
    val name: String,
    val dimension: Dimension,
) : TypeLcaExpression

data class TSubstance(
    val name: String,
    val dimension: Dimension,
    val compartment: String,
    val subCompartment: String? = null,
) : TypeLcaExpression

data class TIndicator(
    val name: String,
    val dimension: Dimension,
) : TypeLcaExpression

data class TImpact(
    val indicator: TIndicator
) : TypeLcaExpression

data class TBioExchange(
    val substance: TSubstance
) : TypeLcaExpression

data class TTechnoExchange(
    val product: TProduct,
) : TypeLcaExpression

data class TProcess(
    val name : String,
    val products: Set<TProduct>,
    val inputs: Set<TProduct>,
    val biosphere: Set<TSubstance>,
) : TypeLcaExpression

data class TSubstanceCharacterization(
    val substance: TSubstance,
    val indicators: Set<TIndicator>,
) : TypeLcaExpression

data class TProcessTemplate(
    val params: Map<String, TQuantity>,
    val body: TProcess,
) : Type