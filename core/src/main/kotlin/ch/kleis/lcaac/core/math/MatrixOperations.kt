/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.math

interface MatrixOperations<Q, M> {
    fun zeros(
        rowDim: Int, colDim: Int,
    ): M

    fun M.rowDim(): Int
    fun M.colDim(): Int

    fun M.negate(): M
    fun M.transpose(): M

    operator fun M.get(row: Int, col: Int): Q
    operator fun M.set(row: Int, col: Int, value: Q)

    fun M.matMul(other: M): M
    fun M.matDiv(other: M): M?
    fun M.matTransposeDiv(other: M): M? {
        return this.transpose().matDiv(other.transpose())?.transpose()
    }
}
