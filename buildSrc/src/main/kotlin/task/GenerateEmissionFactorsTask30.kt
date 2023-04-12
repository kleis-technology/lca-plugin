package task

import net.lingala.zip4j.io.outputstream.ZipOutputStream
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import java.io.FileOutputStream
import java.nio.charset.Charset.defaultCharset
import java.util.zip.GZIPInputStream
import kotlin.streams.asSequence

abstract class GenerateEmissionFactorsTask30 : DefaultTask() {

    @get:Incremental
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    private val csvFormat = CSVFormat.Builder.create().setHeader().build()

    init {
        group = "ch.kleis"
        description = "generateEmissionFactors3_0"
        inputDir.convention(this.project.layout.projectDirectory.dir("src/main/stdlib/ef3.0"))
        outputDir.convention(this.project.layout.buildDirectory.dir("stdlib/ef3.0"))
    }

    @TaskAction
    fun execute(inputChanges: InputChanges) {

        val outputFile = outputDir.file("emissions_factors3.0.jar").get().asFile
        val outputJarStream = ZipOutputStream(FileOutputStream(outputFile))

        val substances = loadAllRecords()
            .map { EF30Record(it) }
            .groupingBy { it.substanceId() }
            .fold({ _: String, element: EF30Record -> Impact() },
                { _: String, accumulator: Impact, element: EF30Record -> accumulator.factor(element) })

        substances.values.groupingBy { it.lcaFileName }
            .fold("package ef30\n\n") { accumulator: String, element: Impact ->
                accumulator.plus(element.fileContent).plus("\n\n")
            }
            .forEach { entry -> generateZipEntry(outputJarStream, entry.key, entry.value) }

        outputJarStream.close()
    }


    private fun loadAllRecords(): Sequence<CSVRecord> {
        val flowCSVParser = CSVParser.parse(
            GZIPInputStream(
                inputDir.file("flows.30.csv.gz")
                    .map { it.asFile }.get().inputStream()
            ), defaultCharset(), csvFormat
        )
        val factorsCSVParser = CSVParser.parse(
            GZIPInputStream(
                inputDir.file("factors.30.csv.gz")
                    .map { it.asFile }.get().inputStream()
            ), defaultCharset(), csvFormat
        )
        return (flowCSVParser.stream().asSequence() +
                factorsCSVParser.stream().asSequence())
    }
}
