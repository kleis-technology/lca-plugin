/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.expression.optics.everyDataRef
import ch.kleis.lcaac.core.lang.expression.optics.everyDataRefInProcess

class Helper<Q> {
    fun substitute(binder: String, value: DataExpression<Q>, body: EProcess<Q>): EProcess<Q> {
        return everyDataRefInProcess<Q>().modify(body) { if (it.name == binder) value else it }
    }

    fun allRequiredRefs(expression: Expression<Q>): Set<String> {
        val allRefs = everyDataRef<Q>() compose EDataRef.name<Q>()
        return allRefs.getAll(expression).toSet()
    }
}
