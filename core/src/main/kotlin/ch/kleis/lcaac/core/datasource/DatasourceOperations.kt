package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.DataSourceExpression
import ch.kleis.lcaac.core.lang.expression.QuantityExpression
import ch.kleis.lcaac.core.lang.expression.StringExpression

interface DataSourceOperations<Q> {
    fun readQuantity(
        source: DataSourceExpression<Q>,
        row: Int,
        column: String,
    ): DataExpression<Q>

    fun readQuantity(
        source: DataSourceExpression<Q>,
        row: String,
        column: String,
    ): DataExpression<Q>

    fun readText(
        source: DataSourceExpression<Q>,
        row: Int,
        column: String,
    ): DataExpression<Q>

    fun readText(
        source: DataSourceExpression<Q>,
        row: String,
        column: String,
    ): DataExpression<Q>
}
