package task

import org.apache.commons.csv.CSVRecord

sealed class EFRecord(val record: CSVRecord) {
    fun type(): String {
        return when (record["FLOW_class0"]) {
            "Emissions" -> "Emission"
            "Resources" -> "Resource"
            "Land use" -> "Land_use"
            else -> throw IllegalStateException("${this} is not proper type")
        }
    }

    fun isSubstance(): Boolean = record.isMapped("FLOW_propertyUnit")

    abstract fun characterizationFactor(): String
    fun unit(): String {
        val raw = if (this.isSubstance()) sanitizeString(record["FLOW_propertyUnit"].trim()) else "u"
        return when (raw) {
            "Item(s)" -> "piece"
            "kg*a" -> "kg*year"
            "m2*a" -> "m2*year"
            "m3*a" -> "m3*year"
            else -> raw
        }
    }

    fun substanceId(): String {
        val id = if (this.subCompartment().isEmpty()) {
            listOf(this.substanceDisplayName(), this.compartment())
        } else {
            listOf(this.substanceDisplayName(), this.compartment(), this.subCompartment())
        }.joinToString()
        return sanitizeString(id).lowercase()
    }

    fun dimension(): String = record["FLOW_property"].trim().lowercase()
    fun lcaFileName(): String = record["FLOW_name"]
        .replace("/", "|").replace("\\", "|")

    fun substanceDisplayName(): String = record["FLOW_name"].replace("\"", "\\\"")
    fun substanceName(): String = sanitizeString(record["FLOW_name"].replace("\"", "\\\""))
    fun casNumber(): String = record["FLOW_casnumber"].trim()
    fun ecNumber(): String = record["FLOW_ecnumber"].trim()
    fun methodName(): String = sanitizeString(record["LCIAMethod_name"].trim())
    fun methodLocation(): String = record["LCIAMethod_location"].trim()

    fun compartment(): String {
        return when (record["FLOW_class1"]) {
            "Emissions to soil" -> "soil"
            "Emissions to water", "Resources from water" -> "water"
            "Emissions to air", "Resources from air" -> "air"
            "Resources from ground" -> "ground"
            "Emissions to industrial soil" -> "industrial soil"
            "Resources from biosphere" -> "biosphere"
            "Land occupation" -> "land occupation"
            "Land transformation" -> "land transformation"
            else -> throw IllegalStateException("$this is not proper compartment")
        }
    }

    fun subCompartment(): String =
        when (record["FLOW_class2"]) {
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
            "Renewable energy resources from biosphere", "Renewable energy resources from ground",
            "Renewable energy resources from air", "Renewable energy resources from water",
            "Renewable material resources from water", "Renewable material resources from biosphere",
            "Renewable material resources from ground" -> "renewable"

            "Other emissions to industrial soil" -> "other"

            else -> throw IllegalStateException("$this is not proper sub-compartment")
        }
}

class EF31Record(record: CSVRecord) : EFRecord(record) {
    override fun characterizationFactor(): String = record["CF EF3.1"]
}

class EF30Record(record: CSVRecord) : EFRecord(record) {
    override fun characterizationFactor(): String = record["LCIAMethod_meanvalue"]
}