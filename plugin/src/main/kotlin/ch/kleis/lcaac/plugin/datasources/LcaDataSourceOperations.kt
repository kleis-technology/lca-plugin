package ch.kleis.lcaac.plugin.datasources

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.QuantityOperations
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.nio.file.Path

class LcaDataSourceOperations<Q>(
    private val ops: QuantityOperations<Q>,
) : DataSourceOperations<Q> {
    override fun read(source: DataSourceExpression<Q>, row: SliceIndex, column: SliceIndex): DataExpression<Q> {
        if (row !is StrIndex || column !is StrIndex) {
            throw EvaluatorException("Not supported yet")
        }
        return when (source) {
            is ECsvSource -> readCsv(source, row, column)
        }
    }

    private val format = CSVFormat.DEFAULT.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .build()

    // TODO: Very inefficient, csv is scanned on every call
    private fun readCsv(source: ECsvSource<Q>, row: StrIndex, column: StrIndex): DataExpression<Q> {
        val schema = source.schema
        val columnType = schema[column.s] ?: throw EvaluatorException("${column.s} not found in schema")

        val file = Path.of(source.location).toFile()
        val index = source.index ?: throw EvaluatorException("Requires index")
        val parser = CSVParser(file.inputStream().reader(), format)
        val rawValue = parser.records.find {
            it[index] == row.s
        }?.get(column.s)
            ?: throw EvaluatorException("value not found (row=${row.s}, col=${column.s})")

        return when(columnType) {
            is CQuantity -> {
                val amount = ops.pure(rawValue.toDouble())
                return EQuantityScale(amount, columnType.unit)
            }
            is CText -> EStringLiteral(rawValue)
        }
    }
}
