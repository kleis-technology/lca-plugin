/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.lang.expression

import arrow.optics.optics

@optics
data class FromProcess<Q>(
    val name: String,
    val matchLabels: MatchLabels<Q>,
    val arguments: Map<String, DataExpression<Q>> = emptyMap(),
) {
    override fun toString(): String {
        return "from $name$matchLabels$arguments"
    }

    companion object
}

@optics
data class MatchLabels<Q>(
    val elements: Map<String, DataExpression<Q>>,
) {
    override fun toString(): String {
        return if (elements.isEmpty()) "" else "$elements"
    }

    companion object
}
