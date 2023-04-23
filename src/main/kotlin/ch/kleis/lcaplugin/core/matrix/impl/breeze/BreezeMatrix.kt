package ch.kleis.lcaplugin.core.matrix.impl.breeze

import breeze.generic.UFunc
import breeze.linalg.CSCMatrix
import breeze.linalg.operators.`HasOps$`
import breeze.linalg.operators.`OpNeg$`
import breeze.linalg.operators.`OpSolveMatrixBy$`
import ch.kleis.lcaplugin.core.matrix.impl.Matrix

class BreezeMatrix(val cscMatrix: CSCMatrix<Double>) : Matrix {
    override fun value(row: Int, col: Int): Double {
        return cscMatrix.apply(row, col)
    }

    override fun add(row: Int, col: Int, value: Double) {
        cscMatrix.update(row, col, cscMatrix.apply(row, col) + value)
    }

    override fun add(other: Matrix): Matrix {
        TODO("Not yet implemented")
    }

    override fun set(row: Int, col: Int, value: Double) {
        cscMatrix.update(row, col, value)
    }

    override fun negate(): Matrix {
        val op: UFunc.UImpl<`OpNeg$`, CSCMatrix<Double>, *> =
            `HasOps$`.`MODULE$`.csc_OpNeg_Double() as UFunc.UImpl<`OpNeg$`, CSCMatrix<Double>, *>
        return BreezeMatrix(this.cscMatrix.`unary_$minus`(op) as CSCMatrix<Double>)
    }

    override fun multiply(other: Matrix): Matrix {
        TODO("Not yet implemented")
    }

    override fun transpose(): Matrix {
        TODO("Not yet implemented")
    }

    override fun rowDim(): Int {
        return this.cscMatrix.rows();
    }

    override fun colDim(): Int {
        return this.cscMatrix.cols()
    }
}