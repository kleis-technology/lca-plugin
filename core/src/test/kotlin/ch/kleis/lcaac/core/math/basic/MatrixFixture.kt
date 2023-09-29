/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.math.basic

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
