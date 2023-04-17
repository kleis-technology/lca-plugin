package task

import net.lingala.zip4j.io.outputstream.ZipOutputStream
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.gradle.api.file.DirectoryProperty
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream
import kotlin.streams.asSequence

class GenerateEmissionFactorsTask<T : EFRecord>(val inputDir: DirectoryProperty, val outputDir: DirectoryProperty) {


    protected val csvFormat = CSVFormat.Builder.create().setHeader().build()

    fun createLibArchive(shortVersion: String, longVersion: String, constructor: (r: CSVRecord) -> T) {

        val outputFile = outputDir.file("emissions_factors$longVersion.jar").get().asFile
        val outputJarStream = ZipOutputStream(FileOutputStream(outputFile))

        createSubstrancesAsString(shortVersion, constructor)
            .forEach { entry ->
                generateZipEntry(outputJarStream, entry.key, entry.value)
            }

        outputJarStream.close()
    }

    fun createSubstrancesAsString(shortVersion: String, constructor: (r: CSVRecord) -> T): Map<String, String> {
        val substances = loadAllRecords(shortVersion)
            .map { constructor(it) }
            .groupingBy { it.substanceId() }
            .fold({ _: String, _: T -> SubstanceWithImpact() },
                { _: String, accumulator: SubstanceWithImpact, element: T -> accumulator.factor(element) })

        return substances.values.groupingBy { it.lcaFileName }
            .fold("package ef$shortVersion\n\n") { accumulator: String, element: SubstanceWithImpact ->
                accumulator.plus(element.fileContent).plus("\n\n")
            }
    }

    private fun loadAllRecords(shortVersion: String): Sequence<CSVRecord> {
        val flowCSVParser = CSVParser.parse(
            GZIPInputStream(
                inputDir.file("flows.$shortVersion.csv.gz")
                    .map { it.asFile }.get().inputStream()
            ), Charset.defaultCharset(), csvFormat
        )
        val factorsCSVParser = CSVParser.parse(
            GZIPInputStream(
                inputDir.file("factors.$shortVersion.csv.gz")
                    .map { it.asFile }.get().inputStream()
            ), Charset.defaultCharset(), csvFormat
        )
        return (flowCSVParser.stream().asSequence() +
                factorsCSVParser.stream().asSequence())
    }
}