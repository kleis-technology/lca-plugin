package task

import org.apache.commons.csv.CSVRecord

sealed class EFRecord(val record: CSVRecord) {
    fun type(): String {
        return when (record["FLOW_class0"]) {
            "Emissions" -> "Emission"
            "Resources" -> "Resource"
            "Land use" -> "Land_use"
            else -> throw IllegalStateException("$this is not proper type")
        }
    }

    fun isSubstance(): Boolean = record.isMapped("FLOW_propertyUnit")

    abstract fun characterizationFactor(): String
    fun unit(): String {
        return when (val raw = if (this.isSubstance()) sanitizeString(record["FLOW_propertyUnit"].trim()) else "u") {
            "Item(s)" -> "piece"
            "kg_a" -> "kga"
            "m2_a" -> "m2a"
            "m3_a" -> "m3a"
            else -> raw
        }
    }

    fun sanitizedSubstanceName(): String {
        return sanitizeString(this.substanceDisplayName()).lowercase()
    }

    fun fullyQualifiedDisplayName(): String {
        return "${substanceDisplayName()}(compartment=${compartment()}, sub_compartment=${subCompartment()}"
    }

    fun lcaFileName(): String = record["FLOW_name"]
        .replace("/", "|").replace("\\", "|")

    fun substanceDisplayName(): String = record["FLOW_name"].replace("\"", "\\\"")
    fun substanceName(): String = sanitizeString(record["FLOW_name"].replace("\"", "\\\""))
    fun casNumber(): String = record["FLOW_casnumber"].trim()
    fun ecNumber(): String = record["FLOW_ecnumber"].trim()
    fun methodName(): String = record["LCIAMethod_name"].trim()
    fun methodLocation(): String = record["LCIAMethod_location"].trim()

    fun compartment(): String {
        return record["FLOW_class1"]
    }

    // FIXME - is this correct ?
    fun subCompartment(): String =
        record["FLOW_class2"]
}

class EF31Record(record: CSVRecord) : EFRecord(record) {
    override fun characterizationFactor(): String = record["CF EF3.1"]
}

class EF30Record(record: CSVRecord) : EFRecord(record) {
    override fun characterizationFactor(): String = record["LCIAMethod_meanvalue"]
}
