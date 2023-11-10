package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.DataSourceExpression

class DataSourceOperationsFixture {
    companion object {
        fun <Q> sourceOps() = object : DataSourceOperations<Q> {
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
    }
}
