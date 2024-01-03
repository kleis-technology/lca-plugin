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

    abstract fun characterizationFactor(): Double
    fun unit(): String {
        return when (val raw = if (this.isSubstance()) sanitize(record["FLOW_propertyUnit"].trim(), toLowerCase = false) else "u") {
            "Item_s" -> "piece"
            "kg_m_a" -> "kga"
            "m2_m_a" -> "m2a"
            "m3_m_a" -> "m3a"
            else -> raw
        }
    }

    fun sanitizedSubstanceName(): String {
        return sanitize(this.substanceDisplayName()).lowercase()
    }

    fun fullyQualifiedDisplayName(): String {
        return "${substanceDisplayName()}(compartment=${compartment()}, sub_compartment=${subCompartment()}"
    }

    fun lcaFileName(): String = record["FLOW_name"]
        .replace("/", "|").replace("\\", "|")

    fun substanceDisplayName(): String = record["FLOW_name"].replace("\"", "\\\"")
    fun substanceName(): String = sanitize(record["FLOW_name"].replace("\"", "\\\""))
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

    protected fun getCF(cell: String): Double {
        val csvValue = record[cell]
        return when {
            csvValue == null || csvValue.isEmpty() -> 0.0
            csvValue.toDoubleOrNull() == null -> throw Exception("Invalid CF value: $csvValue")
            else -> csvValue.toDouble()
        }
    }
}

class EF31Record(record: CSVRecord) : EFRecord(record) {
    override fun characterizationFactor(): Double = getCF("CF EF3.1")
}

class EF30Record(record: CSVRecord) : EFRecord(record) {
    override fun characterizationFactor(): Double = getCF("LCIAMethod_meanvalue")
}
