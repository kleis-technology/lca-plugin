package task

import org.apache.commons.csv.CSVRecord


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
