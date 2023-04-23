package ch.kleis.lcaplugin.core.matrix.impl.breeze

import breeze.linalg.CSCMatrix
import breeze.storage.Zero
import ch.kleis.lcaplugin.core.matrix.impl.Matrix
import ch.kleis.lcaplugin.core.matrix.impl.MatrixFactory
import scala.reflect.ClassTag

class BreezeMatrixFactory : MatrixFactory {
    override fun zero(nRows: Int, nCols: Int): Matrix {
        val classTag = ClassTag.apply<Double>(Double::class.java)
        val cscMatrix = CSCMatrix.zeros(nRows, nCols, classTag, Zero.apply(0.0))
        return BreezeMatrix(cscMatrix)
    }
}