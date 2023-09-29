/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.lang.value

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException

sealed interface ExchangeValue<Q> : Value<Q> {
    fun quantity(): QuantityValue<Q>
    fun port(): MatrixColumnIndex<Q>
}

data class TechnoExchangeValue<Q>(
    val quantity: QuantityValue<Q>,
    val product: ProductValue<Q>,
    val allocation: QuantityValue<Q>? = null,
) : ExchangeValue<Q> {
    init {
        if (quantity.unit.dimension != product.referenceUnit.dimension) {
            throw EvaluatorException("incompatible dimensions: ${quantity.unit.dimension} vs ${product.referenceUnit.dimension} for product ${product.name}")
        }
    }

    override fun quantity(): QuantityValue<Q> {
        return quantity
    }

    override fun port(): MatrixColumnIndex<Q> {
        return product
    }
}

data class BioExchangeValue<Q>(val quantity: QuantityValue<Q>, val substance: SubstanceValue<Q>) : ExchangeValue<Q> {
    init {
        if (quantity.unit.dimension != substance.getDimension()) {
            throw EvaluatorException("incompatible dimensions: ${quantity.unit.dimension} vs ${substance.getDimension()} for substance ${substance.getDisplayName()}, quantity=${quantity.amount}")
        }
    }

    override fun quantity(): QuantityValue<Q> {
        return quantity
    }

    override fun port(): MatrixColumnIndex<Q> {
        return substance
    }
}

data class ImpactValue<Q>(val quantity: QuantityValue<Q>, val indicator: IndicatorValue<Q>) : ExchangeValue<Q> {
    init {
        if (quantity.unit.dimension != indicator.referenceUnit.dimension) {
            throw EvaluatorException("incompatible dimensions: ${quantity.unit.dimension} vs ${indicator.referenceUnit.dimension} for indicator ${indicator.name}, quantity=${quantity.amount}")
        }
    }

    override fun quantity(): QuantityValue<Q> {
        return quantity
    }

    override fun port(): MatrixColumnIndex<Q> {
        return indicator
    }
}
