/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.math

interface QuantityOperations<Q> {
    operator fun Q.plus(other: Q): Q
    operator fun Q.minus(other: Q): Q
    operator fun Q.times(other: Q): Q
    operator fun Q.div(other: Q): Q
    operator fun Q.unaryPlus(): Q = this
    operator fun Q.unaryMinus(): Q

    fun Q.pow(other: Double): Q

    fun Q.toDouble(): Double
    fun pure(value: Double): Q
}

