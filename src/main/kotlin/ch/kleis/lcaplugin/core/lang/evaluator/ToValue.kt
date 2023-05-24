package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.value.*

fun ProcessTemplateExpression.toValue(): ProcessValue {
    return when (this) {
        is EProcessFinal -> this.expression.toValue()
        else -> throw EvaluatorException("$this is not this")
    }
}

fun ESubstanceCharacterization.toValue(): SubstanceCharacterizationValue {
    return SubstanceCharacterizationValue(
        referenceExchange = this.referenceExchange.toValue(),
        impacts = this.impacts.map { it.toValue() },
    )
}

fun EImpact.toValue(): ImpactValue {
    return ImpactValue(
        this.quantity.toValue(),
        this.indicator.toValue(),
    )
}

private fun EIndicatorSpec.toValue(): IndicatorValue {
    val referenceUnit = (this.referenceUnit as? EUnitLiteral)?.toValue()
        ?: throw EvaluatorException("$this has no reference unit")
    return IndicatorValue(
        this.name,
        referenceUnit,
    )
}

fun EProcess.toValue(): ProcessValue {
    return ProcessValue(
        this.name,
        this.products.map { it.toValue() },
        this.inputs.map { it.toValue() },
        this.biosphere.map { it.toValue() },
    )
}

fun EBioExchange.toValue(): BioExchangeValue {
    return BioExchangeValue(
        this.quantity.toValue(),
        this.substance.toValue(),
    )
}

fun ETechnoExchange.toValue(): TechnoExchangeValue {
    return TechnoExchangeValue(
        this.quantity.toValue(),
        this.product.toValue(),
        this.allocation.toValue()
    )
}

fun ESubstanceSpec.toValue(): SubstanceValue {
    val referenceUnit = (this.referenceUnit as? EUnitLiteral)?.toValue()
        ?: throw EvaluatorException("$this has no reference unit")
    val type = this.type ?: return PartiallyQualifiedSubstanceValue(this.name, referenceUnit)
    val compartment = this.compartment ?: return PartiallyQualifiedSubstanceValue(this.name, referenceUnit)
    return FullyQualifiedSubstanceValue(
        this.name,
        type,
        compartment,
        this.subCompartment,
        referenceUnit,
    )
}

fun EProductSpec.toValue(): ProductValue {
    val name = this.name
    val referenceUnitValue = (this.referenceUnit as? EUnitLiteral)?.toValue()
        ?: throw EvaluatorException("$this has no reference unit")
    val fromProcessRefValue = this.fromProcessRef?.toValue()
    return ProductValue(
        name,
        referenceUnitValue,
        fromProcessRefValue,
    )
}

private fun FromProcessRef.toValue(): FromProcessRefValue {
    return FromProcessRefValue(
        this.ref,
        this.arguments.mapValues { it.value.toValue() },
    )
}

fun QuantityExpression.toValue(): QuantityValue =
    when {
        this is EQuantityScale && this.base is EUnitLiteral ->
            QuantityValue(
                this.scale,
                this.base.toValue(),
            )

        this is EUnitLiteral ->
            QuantityValue(1.0, this.toValue())

        else -> throw EvaluatorException("$this is not reduced")
    }

fun EUnitLiteral.toValue(): UnitValue =
    UnitValue(
        this.symbol,
        this.scale,
        this.dimension,
    )

