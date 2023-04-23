package ch.kleis.lcaplugin.core.matrix.impl

import ch.kleis.lcaplugin.core.matrix.impl.breeze.BreezeSolver

interface Solver {
    fun solve(lhs: Matrix, rhs: Matrix): Matrix?
    companion object {
        val INSTANCE: Solver = BreezeSolver()
    }
}
