package ch.kleis.lcaac.core.lang.expression

sealed interface DataSourceExpression<Q> {
    val schema: Map<String, DataExpression<Q>>
    val index: String?
}

data class ECsvSource<Q>(
    val location: String,
    override val schema: Map<String, DataExpression<Q>>,
    override val index: String? = null,
) : DataSourceExpression<Q>
