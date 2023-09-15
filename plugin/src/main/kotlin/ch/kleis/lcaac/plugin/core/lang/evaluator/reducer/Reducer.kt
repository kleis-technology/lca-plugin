package ch.kleis.lcaac.plugin.core.lang.evaluator.reducer

interface Reducer<E> {
    fun reduce(expression: E): E
}
