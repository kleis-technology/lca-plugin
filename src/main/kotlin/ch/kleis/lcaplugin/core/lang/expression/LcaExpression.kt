package ch.kleis.lcaplugin.core.lang.expression

import arrow.optics.optics
import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException

@optics
sealed interface LcaExpression : Expression {
    companion object
}

// Product
@optics
data class EProductSpec(
    val name: String,
    val referenceUnit: UnitExpression? = null,
    val fromProcessRef: FromProcessRef? = null,
) : LcaExpression {
    companion object

    fun withReferenceUnit(referenceUnit: UnitExpression): EProductSpec {
        return EProductSpec(
            name,
            referenceUnit,
            fromProcessRef,
        )
    }

    fun withFromProcessRef(fromProcessRef: FromProcessRef): EProductSpec {
        return EProductSpec(
            name,
            referenceUnit,
            fromProcessRef,
        )
    }
}

// Substance
@optics
sealed interface LcaSubstanceExpression : LcaExpression {
    companion object
}

enum class SubstanceType(val value: String) { // TODO Undefined because of ReduceAndComplete.completeSubstances(), to solve
    EMISSION("Emission"), RESOURCE("Resource"), LAND_USE("Land_use"), UNDEFINED("Undefined");

    companion object {
        private val map = SubstanceType.values().associateBy { it.value }
        infix fun of(value: String): SubstanceType =
            map[value] ?: throw EvaluatorException("Invalid SubstanceType: $value")
    }

    override fun toString(): String {
        return value
    }


}

@optics
data class ESubstance(
    val name: String,
    val displayName: String,
    val type: SubstanceType,
    val compartment: String,
    val subcompartment: String?,
    val referenceUnit: UnitExpression,
) : LcaSubstanceExpression {
    companion object
}

@optics
data class ESubstanceRef(val name: String) : LcaSubstanceExpression, RefExpression {
    override fun name(): String {
        return name
    }

    override fun toString(): String = name

    companion object
}

// Indicator
@optics
sealed interface LcaIndicatorExpression : LcaExpression {
    companion object
}

@optics
data class EIndicator(
    val name: String,
    val referenceUnit: UnitExpression,
) : LcaIndicatorExpression {
    companion object
}

@optics
data class EIndicatorRef(
    val name: String,
) : LcaIndicatorExpression, RefExpression {
    override fun name(): String {
        return name
    }

    override fun toString(): String = name

    companion object
}

// Exchange
@optics
sealed interface LcaExchangeExpression : LcaExpression {
    companion object
}

@optics
data class ETechnoExchange(
    val quantity: QuantityExpression,
    val product: EProductSpec,
    val allocation: QuantityExpression
) :
    LcaExchangeExpression {
    constructor(quantity: QuantityExpression, product: EProductSpec) : this(
        quantity,
        product,
        EQuantityLiteral(100.0, EUnitLiteral("percent", 0.01, Dimension.None))
    )

    companion object
}

@optics
data class EBioExchange(val quantity: QuantityExpression, val substance: LcaSubstanceExpression) :
    LcaExchangeExpression {
    companion object
}

@optics
data class EImpact(val quantity: QuantityExpression, val indicator: LcaIndicatorExpression) : LcaExchangeExpression {
    companion object
}

// Process
@optics
data class EProcess(
    val name: String,
    val products: List<ETechnoExchange>,
    val inputs: List<ETechnoExchange>,
    val biosphere: List<EBioExchange>,
) : LcaExpression {
    companion object
}

// Substance Characterization
@optics
data class ESubstanceCharacterization(
    val referenceExchange: EBioExchange,
    val impacts: List<EImpact>
) : LcaExpression {
    companion object
}

