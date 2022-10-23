package task

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVFormat.DEFAULT
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import java.io.ByteArrayInputStream
import java.nio.charset.Charset.defaultCharset
import java.nio.file.Paths

abstract class GenerateStdLibTask : DefaultTask() {

    @get:Incremental
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        group = "ch.kleis"
        description = "generateStdLib"
        inputDir.convention(this.project.layout.projectDirectory.dir("src/main/stdlib"))
        outputDir.convention(this.project.layout.buildDirectory.dir("stdlib"))
    }

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        val format = CSVFormat.Builder.create().setHeader().build()
        val parser = CSVParser.parse(
            inputDir.file("flows.csv")
                .map { it.asFile }.get(), defaultCharset(), format
        )
        val substances = parser.sortedBy { it[1] }
        val outputFile = outputDir.file("substances.jar").get().asFile
        val outputJarFile = ZipFile(outputFile)
        var currentFileName = substances[0].lcaFileName()
        var fileContent = ""
        for(record in substances) {
            if(record.lcaFileName() != currentFileName) {
                generateZipEntry(outputJarFile, currentFileName, fileContent)
                currentFileName = record.lcaFileName()
                fileContent = ""
            }
            fileContent = fileContent.plus(generateLcaFile(record)).plus("\n\n")
        }
        outputJarFile.close()
    }

    fun generateLcaFile(csvRecord: CSVRecord): String {
        val escapedName = csvRecord[1].replace("\"", "\\\"")
        return """
            substance "$escapedName", "${csvRecord[5].toCompartment()}", "${csvRecord[6].toSubCompartment()}" {
                
                type: ${csvRecord[4].toType()}
                unit: ${csvRecord[8]}
                
                meta {
                    - dimension: "${csvRecord[7].trim()}"
                    - generator: "kleis-lca-generator"
                    - casNumber: "${csvRecord[2].trim()}"
                    - ecNumber: "${csvRecord[3].trim()}"
                }
            }
        """.trimIndent()
    }

    fun generateZipEntry(zipFile: ZipFile, currentFileName: String, zipEntryContent: String) {
        val parameters = ZipParameters()
        parameters.fileNameInZip = "$currentFileName.lca"
        zipFile.addStream(zipEntryContent.byteInputStream(), parameters)
    }
}

fun String.toType(): String {
    return when (this) {
        "Emissions" -> "emissions"
        "Resources" -> "resources"
        "Land use" -> "land_use"
        else -> throw IllegalStateException("${this} is not proper type")
    }
}

fun String.toCompartment(): String {
    return when (this) {
        "Emissions to soil" -> "soil"
        "Emissions to water", "Resources from water" -> "water"
        "Emissions to air", "Resources from air" -> "air"
        "Resources from ground" -> "ground"
        "Emissions to industrial soil" -> "industrial soil"
        "Resources from biosphere" -> "biosphere"
        "Land occupation" -> "land occupation"
        "Land transformation" -> "land transformation"
        else -> throw IllegalStateException("${this} is not proper compartment")
    }
}

fun String.toSubCompartment(): String { // TODO : do a correct mapping ...
    return this
}

fun CSVRecord.lcaFileName(): String = this[1].replace("/", "|");
