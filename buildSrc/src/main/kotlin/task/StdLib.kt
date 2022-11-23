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

fun CSVRecord.subCompartment(): String =
    when (this["FLOW_class2"]) {
        "Emissions to non-agricultural soil" -> "non-agricultural"
        "Emissions to water, unspecified (long-term)" -> "long-term"

        "Emissions to agricultural soil" -> "agricultural"
        "Emissions to non-urban air or from high stacks",
        "Emissions to non-urban air high stack" -> "non-urban high stack"

        "Emissions to non-urban air low stack" -> "non-urban low stack"
        "Emissions to non-urban air very high stack" -> "non-urban very high stack"
        "Emissions to non-urban air close to ground" -> "non-urban close ground"

        "Emissions to urban air high stack" -> "urban high stack"
        "Emissions to urban air very high stack" -> "urban very high stack"
        "Emissions to urban air low stack" -> "urban low stack"
        "Emissions to urban air close to ground" -> "urban air close to ground"
        "Emissions to air, indoor" -> "indoor"
        "Emissions to air, unspecified (long-term)" -> "long-term"
        "Emissions to soil, unspecified", "Emissions to water, unspecified", "Emissions to air, unspecified", "" -> ""
        "Emissions to sea water" -> "sea water"
        "Emissions to fresh water" -> "fresh water"
        "Emissions to lower stratosphere and upper troposphere" -> "lower stratosphere and upper troposphere"
        "Non-renewable energy resources from ground", "Non-renewable element resources from ground",
        "Non-renewable material resources from ground", "Non-renewable element resources from water",
        "Non-renewable material resources from water" -> "non-renewable"

        "Renewable material resources from air", "Renewable element resources from air",
        "Renewable energy resources from biosphere" , "Renewable energy resources from ground",
        "Renewable energy resources from air", "Renewable energy resources from water",
        "Renewable material resources from water", "Renewable material resources from biosphere",
        "Renewable material resources from ground" -> "renewable"

        "Other emissions to industrial soil" -> "other"

        else -> throw IllegalStateException("${this} is not proper sub-compartment")
    }


fun CSVRecord.isSubstance(): Boolean = this.isMapped("FLOW_propertyUnit")

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

fun CSVRecord.substanceId(): String {
    var id = if (this.subCompartment().isEmpty()) {
        listOf(this.substanceName(), this.compartment())
    } else {
        listOf(this.substanceName(), this.compartment(), this.subCompartment())
    }.joinToString()
    return "\"$id\""
}



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

    private fun padStart(str: String, pad: Int): String {
        return str.padStart(str.length + pad)
    }

    fun factorsContent(pad: Int = 4): String = factorRecords
        .map {
            "|".plus(
                padStart(
                    "- \"${it.methodName()}\" ${it.methodLocation()} ${it.characterizationFactor()}",
                    pad
                )
            )
        }
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
            |    name: "${substanceRecord?.substanceName()}"
            |    compartment: "${substanceRecord?.compartment()}"
            |    sub_compartment: "${substanceRecord?.subCompartment()}"
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
