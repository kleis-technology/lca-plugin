package ch.kleis.lcaplugin.core.lang.evaluator.reducer

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.EUnitAlias

class InfiniteLoopChecker {
    private val unitAliasRegister = arrayListOf<EUnitAlias>()

    fun check(expression: EUnitAlias){
        if (unitAliasRegister.contains(expression)){
            throw EvaluatorException("Recursive dependency for unit ${expression.symbol}");
        }
        unitAliasRegister.add(expression)
    }

    fun clearTraceAlias(){
        unitAliasRegister.clear()
    }
}