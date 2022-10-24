package task

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import java.nio.charset.Charset.defaultCharset
import java.util.zip.GZIPInputStream

abstract class GenerateEmissionFactorsTask : DefaultTask() {

    @get:Incremental
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        group = "ch.kleis"
        description = "generateEmissionFactors"
        inputDir.convention(this.project.layout.projectDirectory.dir("src/main/stdlib"))
        outputDir.convention(this.project.layout.buildDirectory.dir("stdlib"))
    }

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        val format = CSVFormat.Builder.create().setHeader().build()
        val parser = CSVParser.parse(
            GZIPInputStream(inputDir.file("factors.csv.gz")
                .map { it.asFile }.get().inputStream()), defaultCharset(), format
        )
        val substances = parser.sortedBy { it[1] }
        val outputFile = outputDir.file("emissions_factors.jar").get().asFile
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

        """
            
        """.trimIndent()
    }

    fun generateZipEntry(zipFile: ZipFile, currentFileName: String, zipEntryContent: String) {
        val parameters = ZipParameters()
        parameters.fileNameInZip = "$currentFileName.lca"
        zipFile.addStream(zipEntryContent.byteInputStream(), parameters)
    }
}
