package ch.kleis.lcaac.plugin.datasources

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.DataSourceExpression
import ch.kleis.lcaac.core.math.QuantityOperations

class LcaDataSourceOperations<Q>(
    private val ops: QuantityOperations<Q>,
) : DataSourceOperations<Q> {
    override fun readQuantity(source: DataSourceExpression<Q>, row: Int, column: String): DataExpression<Q> {
        TODO("Not yet implemented")
    }

    override fun readQuantity(source: DataSourceExpression<Q>, row: String, column: String): DataExpression<Q> {
        TODO("Not yet implemented")
    }

    override fun readText(source: DataSourceExpression<Q>, row: Int, column: String): DataExpression<Q> {
        TODO("Not yet implemented")
    }

    override fun readText(source: DataSourceExpression<Q>, row: String, column: String): DataExpression<Q> {
        TODO("Not yet implemented")
    }
}
