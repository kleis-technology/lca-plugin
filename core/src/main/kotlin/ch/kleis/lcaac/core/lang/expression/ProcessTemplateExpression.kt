/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.lang.expression

import arrow.optics.optics

@optics
sealed interface ProcessTemplateExpression<Q> : Expression<Q> {
    companion object
}

@optics
data class EProcessTemplate<Q>(
    val params: Map<String, DataExpression<Q>> = emptyMap(),
    val locals: Map<String, DataExpression<Q>> = emptyMap(),
    val body: EProcess<Q>,
) : ProcessTemplateExpression<Q> {
    companion object
}

@optics
data class EProcessTemplateApplication<Q>(
    val template: EProcessTemplate<Q>,
    val arguments: Map<String, DataExpression<Q>> = emptyMap()
) : ProcessTemplateExpression<Q> {
    companion object
}

@optics
data class EProcessFinal<Q>(val expression: EProcess<Q>) : ProcessTemplateExpression<Q> {
    companion object
}


