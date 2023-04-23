package ch.kleis.lcaplugin.core.matrix.impl

import ch.kleis.lcaplugin.core.matrix.impl.breeze.BreezeMatrixFactory

interface MatrixFactory {
    fun zero(nRows: Int, nCols: Int): Matrix
    companion object {
        val INSTANCE: MatrixFactory =
            BreezeMatrixFactory()
    }
}
