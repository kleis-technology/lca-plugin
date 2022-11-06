package task

import net.lingala.zip4j.io.outputstream.ZipOutputStream
import net.lingala.zip4j.model.ZipParameters
import org.apache.commons.csv.CSVRecord


fun CSVRecord.type(): String {
    return when (this["FLOW_class0"]) {
        "Emissions" -> "emissions"
        "Resources" -> "resources"
        "Land use" -> "land_use"
        else -> throw IllegalStateException("${this} is not proper type")
    }
}

fun CSVRecord.compartment(): String {
    return when (this["FLOW_class1"]) {
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

fun CSVRecord.isSubstance(): Boolean = this.isMapped("FLOW_propertyUnit")
fun CSVRecord.subCompartment(): String = this["FLOW_class2"]
fun CSVRecord.unit(): String = this["FLOW_propertyUnit"].trim()
fun CSVRecord.dimension(): String = this["FLOW_property"].trim()
fun CSVRecord.lcaFileName(): String = this["FLOW_name"]
    .replace("/", "|").replace("\\", "|");

fun CSVRecord.substanceName(): String = this["FLOW_name"].replace("\"", "\\\"")
fun CSVRecord.casNumber(): String = this["FLOW_casnumber"].trim()
fun CSVRecord.ecNumber(): String = this["FLOW_ecnumber"].trim()

fun CSVRecord.characterizationFactor(): String = this["CF EF3.1"]
fun CSVRecord.methodName(): String = this["LCIAMethod_name"].trim()
fun CSVRecord.methodLocation(): String = this["LCIAMethod_location"].trim()

fun CSVRecord.substanceId(): String = """
    "${this.substanceName()}", "${this.compartment()}", "${this.subCompartment()}"
""".trimIndent()

fun generateZipEntry(outputStream: ZipOutputStream, currentFileName: String, zipEntryContent: String) {
    val parameters = ZipParameters()
    parameters.fileNameInZip = "$currentFileName.lca"
    outputStream.putNextEntry(parameters);
    outputStream.write(zipEntryContent.encodeToByteArray());
    outputStream.closeEntry();
}

internal class Impact() {

    val factorRecords = mutableListOf<CSVRecord>()
    var substanceRecord: CSVRecord? = null

    fun factor(element: CSVRecord): Impact {
        if (element.isSubstance()) {
            substanceRecord = element
        } else {
            factorRecords.add(element)
        }
        return this;
    }

    private fun padStart(str: String, pad: Int) : String {
        return str.padStart(str.length + pad)
    }

    fun factorsContent(pad: Int = 4): String = factorRecords
        .map { "|".plus(padStart("- \"${it.methodName()}\" ${it.methodLocation()} ${it.characterizationFactor()}", pad)) }
        .joinToString("\n")

    val factorsSection: String
        get() = if (factorRecords.size > 0) {
            """
            |factors ${factorRecords.first().substanceId()} : ef31 {
            ${factorsContent()}    
            |}
        """.trimMargin()
        } else ""

    val factorsSubSection: String
        get() = if (factorRecords.size > 0) {
            """
            |    factors : ef31 {
            ${factorsContent(8)}    
            |    }
        """.trimMargin()
        } else ""

    val substanceContent: String
        get() = """
            |substance ${substanceRecord?.substanceId()} {
            |
            |$substanceBody    
            |
            |}
        """.trimMargin()

    val substanceBody: String
        get() = """
            |    type: ${substanceRecord?.type()}
            |    unit: ${substanceRecord?.unit()}
            |    
            |$factorsSubSection
            |    
            |    meta {
            |        - dimension: "${substanceRecord?.dimension()}"
            |        - generator: "kleis-lca-generator"
            |        - casNumber: "${substanceRecord?.casNumber()}"
            |        - ecNumber: "${substanceRecord?.ecNumber()}"
            |    }
            """.trimMargin()

    val fileContent: String
        get() = substanceRecord?.run { substanceContent } ?: factorsSection

    val lcaFileName: String
        get() = substanceRecord?.lcaFileName() ?: factorRecords.first().lcaFileName()

}
