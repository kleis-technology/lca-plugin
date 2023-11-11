package ch.kleis.lcaac.plugin.datasources

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.DataSourceExpression
import ch.kleis.lcaac.core.lang.expression.SliceIndex
import ch.kleis.lcaac.core.math.QuantityOperations

class LcaDataSourceOperations<Q>(
    private val ops: QuantityOperations<Q>,
) : DataSourceOperations<Q> {
    override fun read(source: DataSourceExpression<Q>, row: SliceIndex, column: SliceIndex): DataExpression<Q> {
        TODO("Not yet implemented")
    }
}
