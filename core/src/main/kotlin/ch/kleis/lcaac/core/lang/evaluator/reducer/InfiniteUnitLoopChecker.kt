/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.lang.evaluator.reducer

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.EUnitAlias

class InfiniteUnitLoopChecker<Q> {
    private val unitAliasRegister = mutableSetOf<EUnitAlias<Q>>()

    fun check(expression: EUnitAlias<Q>) {
        if (unitAliasRegister.contains(expression)) {
            throw EvaluatorException("Recursive dependency for unit ${expression.symbol}")
        }
        unitAliasRegister.add(expression)
    }

    fun clearTraceAlias() {
        unitAliasRegister.clear()
    }
}
