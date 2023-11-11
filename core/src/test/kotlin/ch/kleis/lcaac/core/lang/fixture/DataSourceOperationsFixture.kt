package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.DataSourceExpression
import ch.kleis.lcaac.core.lang.expression.SliceIndex

class DataSourceOperationsFixture {
    companion object {
        fun <Q> sourceOps() = object : DataSourceOperations<Q> {
            override fun read(source: DataSourceExpression<Q>, row: SliceIndex, column: SliceIndex): DataExpression<Q> {
                TODO("Not yet implemented")
            }
        }
    }
}
