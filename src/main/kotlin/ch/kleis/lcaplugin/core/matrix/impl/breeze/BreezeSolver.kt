package ch.kleis.lcaplugin.core.matrix.impl.breeze

import breeze.generic.UFunc
import breeze.generic.`UFunc$`
import breeze.linalg.CSCMatrix
import breeze.linalg.operators.`HasOps$`
import breeze.linalg.operators.`OpSolveMatrixBy$`
import ch.kleis.lcaplugin.core.matrix.impl.Matrix
import ch.kleis.lcaplugin.core.matrix.impl.Solver

class BreezeSolver : Solver {
    override fun solve(lhs: Matrix, rhs: Matrix): Matrix? {
        val blhs = lhs as BreezeMatrix
        val brhs = rhs as BreezeMatrix

        val op: UFunc.UImpl2<`OpSolveMatrixBy$`, CSCMatrix<Double>, CSCMatrix<Double>, *> =
            `HasOps$`.`MODULE$`.impl_OpSolveMatrixBy_CSC_CSC_eq_CSC() as UFunc.UImpl2<`OpSolveMatrixBy$`, CSCMatrix<Double>, CSCMatrix<Double>, *>
        return BreezeMatrix(blhs.cscMatrix.`$bslash`(brhs.cscMatrix, op) as CSCMatrix<Double>)
    }
}