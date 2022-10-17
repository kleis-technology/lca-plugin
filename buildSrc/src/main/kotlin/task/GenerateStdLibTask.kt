package task

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
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
        group = "com.kleis"
        description = "generateStdLib"
        inputDir.convention(this.project.layout.projectDirectory.dir("src/main/stdlib"))
        outputDir.convention(this.project.layout.buildDirectory.dir("stdlib"))
    }

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        val parser = CSVParser.parse(inputDir.file("flows.csv").map { it.asFile }.get(), defaultCharset(), DEFAULT)
        val substances = parser.distinctBy { it[1] }
        val outputFile = outputDir.file("substances.jar").get().asFile
        val outputJarFile = ZipFile(outputFile)
        substances.forEach { generateZipEntry(outputJarFile, it) }
        outputJarFile.close()
    }

    fun generateLcaFile(csvRecord: CSVRecord): String {
        val escapedName = csvRecord[1].replace("\"", "\\\"")
        return """
            substance "$escapedName" {
                meta {
                    - generator: "kleis-lca-generator"
                    - casNumber: "${csvRecord[2]}"
                    - ecNumber: "${csvRecord[3]}"
                }
            }
        """.trimIndent()
    }

    fun generateZipEntry(zipFile: ZipFile, csvRecord: CSVRecord) {
        val fileName = csvRecord[1].replace("/", "|");
        val parameters = ZipParameters()
        parameters.fileNameInZip = "$fileName.lca"
        zipFile.addStream(generateLcaFile(csvRecord).byteInputStream(), parameters)
    }
}