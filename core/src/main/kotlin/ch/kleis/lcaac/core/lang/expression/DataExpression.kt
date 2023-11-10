package ch.kleis.lcaac.core.lang.expression

import arrow.optics.optics
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol

@optics
sealed interface DataExpression<Q> {
    companion object
}

sealed interface QuantityExpression<Q>
sealed interface StringExpression

@optics
data class EDataRef<Q>(val name: String) : DataExpression<Q> {
    fun name(): String {
        return name
    }

    override fun toString(): String {
        return name
    }

    companion object
}

/*
    Quantities
 */

@optics
data class EQuantityFrom<Q>(
    val source: String,
    val row: Int,
    val column: String,
) : DataExpression<Q>, QuantityExpression<Q> {
    companion object
}

@optics
data class EQuantityFromIndexed<Q>(
    val source: String,
    val row: String,
    val column: String,
) : DataExpression<Q>, QuantityExpression<Q> {
    companion object
}

@optics
data class EUnitLiteral<Q>(val symbol: UnitSymbol, val scale: Double, val dimension: Dimension) : DataExpression<Q>,
    QuantityExpression<Q> {
    override fun toString(): String {
        return symbol.toString()
    }

    companion object
}

@optics
data class EUnitAlias<Q>(val symbol: String, val aliasFor: DataExpression<Q>) : DataExpression<Q>, QuantityExpression<Q> {
    companion object
}

@optics
data class EQuantityScale<Q>(val scale: Q, val base: DataExpression<Q>) : DataExpression<Q>, QuantityExpression<Q> {
    override fun toString(): String {
        return "$scale $base"
    }

    companion object
}

@optics
data class EUnitOf<Q>(val expression: DataExpression<Q>) : DataExpression<Q>, QuantityExpression<Q> {
    companion object
}

@optics
data class EQuantityAdd<Q>(val leftHandSide: DataExpression<Q>, val rightHandSide: DataExpression<Q>) : DataExpression<Q>, QuantityExpression<Q> {
    companion object
}

@optics
data class EQuantitySub<Q>(val leftHandSide: DataExpression<Q>, val rightHandSide: DataExpression<Q>) : DataExpression<Q>, QuantityExpression<Q> {
    companion object
}

@optics
data class EQuantityMul<Q>(val leftHandSide: DataExpression<Q>, val rightHandSide: DataExpression<Q>) : DataExpression<Q>, QuantityExpression<Q> {
    companion object
}

@optics
data class EQuantityDiv<Q>(val leftHandSide: DataExpression<Q>, val rightHandSide: DataExpression<Q>) : DataExpression<Q>, QuantityExpression<Q> {
    companion object
}

@optics
data class EQuantityPow<Q>(val quantity: DataExpression<Q>, val exponent: Double) : DataExpression<Q>, QuantityExpression<Q> {
    companion object
}

@optics
data class EQuantityClosure<Q>(
    val symbolTable: SymbolTable<Q>, val expression: DataExpression<Q>
) : DataExpression<Q>, QuantityExpression<Q> {
    companion object
}

/*
    String
 */

@optics
data class EStringLiteral<Q>(val value: String) : DataExpression<Q>, StringExpression {
    override fun toString(): String {
        return value
    }

    companion object
}
@optics
data class EStringFrom<Q>(
    val source: String,
    val row: Int,
    val column: String,
) : DataExpression<Q>, StringExpression {
    companion object
}

@optics
data class EStringFromIndexed<Q>(
    val source: String,
    val row: String,
    val column: String,
) : DataExpression<Q>, StringExpression {
    companion object
}
