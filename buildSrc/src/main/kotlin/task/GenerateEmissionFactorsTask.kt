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
            .fold({ _: String, _: T -> SubstanceWithImpactAccumulator() },
                { _: String, accumulator: SubstanceWithImpactAccumulator, element: T -> accumulator.addElement(element) })
        val dict = listOf("Name;Type;Compartment;SubCompartment").plus(
            substances.values.asSequence()
                .filter { it.substanceName.isNotBlank() }
                .map { "${it.substanceName};${it.substanceType};${it.substanceCompartment};${it.substanceSubCompartment}" }
        ).joinToString("\n")
        return substances.values.groupingBy { it.lcaFileName }
            .fold("package ef$shortVersion\n\n") { accumulator: String, element: SubstanceWithImpactAccumulator ->
                accumulator.plus(element.fileContent).plus("\n\n")
            }.plus("META-INF/dictionary.csv" to dict)
    }

    private fun loadAllRecords(shortVersion: String): Sequence<CSVRecord> {
        val flowCSVSequence = getCSVSequence("flows.$shortVersion.csv.gz")
        val factorsCSVSequence = getCSVSequence("factors.$shortVersion.csv.gz")
        return (flowCSVSequence + factorsCSVSequence)
    }

    private fun getCSVSequence(fileName: String): Sequence<CSVRecord> {
        return CSVParser.parse(
            GZIPInputStream(
                inputDir.file(fileName)
                    .map { it.asFile }.get().inputStream()
            ), Charset.defaultCharset(), csvFormat
        ).stream().asSequence()
    }
}