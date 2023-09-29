/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.lang.value

sealed interface DataValue<Q> : Value<Q>

data class StringValue<Q>(val s: String) : DataValue<Q> {
    override fun toString(): String {
        return s
    }
}

data class QuantityValue<Q>(val amount: Q, val unit: UnitValue<Q>) : DataValue<Q> {
    override fun toString(): String {
        return "$amount ${unit.symbol}"
    }
}
