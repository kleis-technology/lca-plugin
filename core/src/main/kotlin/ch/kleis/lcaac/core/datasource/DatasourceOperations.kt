package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.DataSourceExpression
import ch.kleis.lcaac.core.lang.expression.SliceIndex

interface DataSourceOperations<Q> {
    fun read(
        source: DataSourceExpression<Q>,
        row: SliceIndex,
        column: SliceIndex,
    ): DataExpression<Q>
}
