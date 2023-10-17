package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.QuantityOperations

class ToValue<Q>(
    private val ops: QuantityOperations<Q>,
) {
    fun ProcessTemplateExpression<Q>.toValue(): ProcessValue<Q> {
        return when (this) {
            is EProcessFinal -> this.expression.toValue()
            else -> throw EvaluatorException("$this is not final")
        }
    }

    private fun doubleValueOf(q: () -> Q): Double {
        return with(ops) { q().toDouble() }
    }

    private fun EProcess<Q>.toValue(): ProcessValue<Q> {
        return ProcessValue(
            this.name,
            this.labels.mapValues { it.value.toValue() as StringValue<Q> },
            this.products.map { it.toValue() },
            this.inputs.map { it.toValue() },
            this.biosphere.map { it.toValue() },
            this.impacts.map { it.toValue() }
        )
    }

    fun DataExpression<Q>.toValue(): DataValue<Q> {
        return when (this) {
            is EStringLiteral -> StringValue(this.value)

            is EQuantityScale -> when (val b = this.base) {
                is EUnitLiteral -> QuantityValue(
                    this.scale, b.toUnitValue(),
                )

                else -> throw EvaluatorException("$b is not reduced")
            }

            is EGuardedExpression -> checkGuardedExpression(this)

            else -> throw EvaluatorException("$this is not reduced")
        }
    }

    private fun checkGuardedExpression(guardedExpr: EGuardedExpression<Q>): DataValue<Q> {
        // differentiate between StringValue (illegal) and QuantityValue (legal)
        val expression = guardedExpr.expression.toValue() as? QuantityValue
            ?: throw EvaluatorException("Cannot guard non-numerical expression ${guardedExpr.expression.toValue()}")
        val low = guardedExpr.low.toValue() as? QuantityValue
            ?: throw EvaluatorException("Lower bound must be numerical in $this@ToValue")
        val high = guardedExpr.high.toValue() as? QuantityValue
            ?: throw EvaluatorException("Higher bound must be numerical in $this@ToValue")

        return when (listOf(expression, low, high).map { it.unit.dimension }.distinct().size) {
            1 -> {
                val dLow = doubleValueOf { low.amount }
                val dHigh = doubleValueOf { high.amount }
                val dExpr = doubleValueOf { expression.amount }
                if (dExpr in dLow..dHigh) {
                    expression
                } else {
                    throw EvaluatorException("Bounds are not respected in guard: $dExpr not between $dLow and $dHigh")
                }
            }
            else -> throw EvaluatorException("Incompatible dimensions: ${expression.unit.dimension} between ${low.unit.dimension} and ${high.unit.dimension}")
        }
    }

    fun ETechnoExchange<Q>.toValue(): TechnoExchangeValue<Q> {
        return TechnoExchangeValue(
            this.quantity.toValue() as QuantityValue<Q>,
            this.product.toValue(),
            this.allocation?.toValue() as QuantityValue<Q>?,
        )
    }

    private fun EBioExchange<Q>.toValue(): BioExchangeValue<Q> {
        return BioExchangeValue(
            this.quantity.toValue() as QuantityValue<Q>,
            this.substance.toValue(),
        )
    }

    private fun EImpact<Q>.toValue(): ImpactValue<Q> {
        return ImpactValue(
            this.quantity.toValue() as QuantityValue<Q>,
            this.indicator.toValue(),
        )
    }

    fun EProductSpec<Q>.toValue(): ProductValue<Q> {
        val name = this.name
        @Suppress("UNCHECKED_CAST")
        val referenceUnitValue = (this.referenceUnit as QuantityExpression<Q>?)
            ?.toUnitValue()
            ?: throw EvaluatorException("$this has no reference unit")
        val fromProcessRefValue = this.fromProcess?.toValue()
        return ProductValue(
            name,
            referenceUnitValue,
            fromProcessRefValue,
        )
    }

    fun QuantityExpression<Q>.toUnitValue(): UnitValue<Q> =
        when {
            this is EQuantityScale && this.base is EUnitLiteral ->
                UnitValue(
                    base.symbol.scale(doubleValueOf { scale }),
                    doubleValueOf { scale } * base.scale,
                    base.dimension,
                )

            this is EUnitLiteral ->
                UnitValue(
                    this.symbol,
                    this.scale,
                    this.dimension,
                )

            else -> throw EvaluatorException("$this is not reduced")
        }

    fun ESubstanceSpec<Q>.toValue(): SubstanceValue<Q> {
        @Suppress("UNCHECKED_CAST")
        val referenceUnit = (this.referenceUnit as QuantityExpression<Q>?)
            ?.toUnitValue()
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

    private fun EIndicatorSpec<Q>.toValue(): IndicatorValue<Q> {
        @Suppress("UNCHECKED_CAST")
        val referenceUnit = (this.referenceUnit as QuantityExpression<Q>?)
            ?.toUnitValue()
            ?: throw EvaluatorException("$this has no reference unit")
        return IndicatorValue(
            this.name,
            referenceUnit,
        )
    }


    fun ESubstanceCharacterization<Q>.toValue(): SubstanceCharacterizationValue<Q> {
        return SubstanceCharacterizationValue(
            referenceExchange = this.referenceExchange.toValue(),
            impacts = this.impacts.map { it.toValue() },
        )
    }

    private fun FromProcess<Q>.toValue(): FromProcessRefValue<Q> {
        return FromProcessRefValue(
            this.name,
            this.matchLabels.elements.mapValues { it.value.toValue() as StringValue },
            this.arguments.mapValues {
                when (val e = it.value) {
                    is QuantityExpression<*> -> e.toValue()
                    is StringExpression -> e.toValue()
                    is EDataRef -> throw EvaluatorException("$it is not reduced")
                    is EGuardedExpression -> checkGuardedExpression(e)
                }
            },
        )
    }


}


