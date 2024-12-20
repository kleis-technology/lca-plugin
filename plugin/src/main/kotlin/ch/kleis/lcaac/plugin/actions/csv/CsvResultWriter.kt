package ch.kleis.lcaac.plugin.actions.csv

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.Closeable
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer

class CsvResultWriter(
    outputStream: OutputStream,
    private val writer: Writer = OutputStreamWriter(outputStream)
) : Closeable {
    private val format = CSVFormat.DEFAULT.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .setRecordSeparator(System.lineSeparator())
        .build()
    private val csvPrinter = CSVPrinter(writer, format)

    fun write(results: List<CsvResult>) {
        val first = results.firstOrNull() ?: return
        val header = first.request.columns()
            .plus(listOf("product", "reference unit"))
            .plus(first.impacts.toList().map { "${it.first.getShortName()} [${it.first.referenceUnit().symbol}]" })
        csvPrinter.printRecord(header)

        results.forEach { result ->
            val line = result.request.arguments()
                .plus(listOf(result.output.getShortName(), result.output.referenceUnit().symbol))
                .plus(result.impacts.toList().map { it.second.amount.toString() })
            csvPrinter.printRecord(line)
        }
    }

    override fun close() {
        writer.close()
    }
}
