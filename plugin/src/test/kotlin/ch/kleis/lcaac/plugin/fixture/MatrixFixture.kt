package ch.kleis.lcaac.plugin.fixture

import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicOperations

class MatrixFixture {
    companion object {
        fun basic(rows: Int, cols: Int, data: Array<Double>): BasicMatrix {
            with(BasicOperations) {
                val a = zeros(rows, cols)
                for (row in 0 until rows) {
                    for (col in 0 until cols) {
                        if(data[cols * row + col] != 0.0) {
                            a[row, col] = pure(data[cols * row + col])
                        }
                    }
                }
                return a
            }
        }
    }
}
