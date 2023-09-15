package ch.kleis.lcaac.plugin.core.matrix

import ch.kleis.lcaac.plugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.plugin.core.lang.value.ProcessValue
import ch.kleis.lcaac.plugin.core.lang.value.QuantityValueOperations
import ch.kleis.lcaac.plugin.core.math.Operations

class DemandMatrix<Q, M>(
    targetProcess: ProcessValue<Q>,
    observablePorts: IndexedCollection<MatrixColumnIndex<Q>>,
    ops: Operations<Q, M>,
) {
    val data: M = ops.zeros(1, observablePorts.size())

    init {
        val quantityOps = QuantityValueOperations(ops)
        with(ops) {
            targetProcess.products.forEach {
                val col = observablePorts.indexOf(it.product)
                val value = with(quantityOps) { it.quantity.absoluteScaleValue() }
                data[0, col] = data[0, col] + value
            }
        }
    }
}
