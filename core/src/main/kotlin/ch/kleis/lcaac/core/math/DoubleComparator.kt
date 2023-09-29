/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.math

import kotlin.math.absoluteValue
import kotlin.math.pow

class DoubleComparator {
    companion object {
        private val machineEpsilon = 2.0.pow(-53.0)
        val ACCEPTABLE_RELATIVE_ERROR = 8 * machineEpsilon
        fun nzEquals(a: Double, b: Double): Boolean {
            return (a - b).absoluteValue / maxOf(a.absoluteValue, b.absoluteValue) <= ACCEPTABLE_RELATIVE_ERROR
        }
    }
}
