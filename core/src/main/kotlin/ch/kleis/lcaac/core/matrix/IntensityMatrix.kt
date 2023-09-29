/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.matrix

import ch.kleis.lcaac.core.lang.value.MatrixRowIndex
import ch.kleis.lcaac.core.lang.value.QuantityValue
import ch.kleis.lcaac.core.lang.value.UnitValue
import ch.kleis.lcaac.core.math.Operations

class IntensityMatrix<Q, M>(
    private val connections: IndexedCollection<MatrixRowIndex<Q>>,
    private val data: M,
    private val ops: Operations<Q, M>,
    ) {
    fun intensityOf(port: MatrixRowIndex<Q>): QuantityValue<Q> {
        with(ops) {
            val unit = UnitValue.none<Q>()
            val amount = data[0, connections.indexOf(port)]
            return QuantityValue(amount, unit)
        }
    }
}
