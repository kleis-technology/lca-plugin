package ch.kleis.lcaac.core.lang.type

import ch.kleis.lcaac.core.lang.dimension.Dimension

sealed interface Type

data class TUnit(val dimension: Dimension) : Type

sealed interface TypeDataExpression : Type
object TString : TypeDataExpression {
    override fun toString(): String {
        return "TString"
    }
}

data class TQuantity(val dimension: Dimension) : TypeDataExpression


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

data class TBioExchange(
    val substance: TSubstance
) : TypeLcaExpression

data class TTechnoExchange(
    val product: TProduct,
) : TypeLcaExpression
