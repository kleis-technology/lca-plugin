package ch.kleis.lcaac.plugin.imports.ecospold.lci

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.Reader
import kotlin.streams.asSequence

typealias ID = String

sealed interface MappingExchange {
    val elementaryExchangeId: String
    val comment: String
}

data class OrphanMappingExchange(
    override val elementaryExchangeId: String,
    override val comment: String = "",
) : MappingExchange {
    constructor(elementaryExchangeId: String) : this(
        elementaryExchangeId,
        "Ecoinvent orphan. Ecoinvent ID: $elementaryExchangeId"
    )
}

data class UnknownMappingExchange(
    override val elementaryExchangeId: String,
    override val comment: String = "",
) : MappingExchange {
    constructor(elementaryExchangeId: String) : this(
        elementaryExchangeId,
        "Flow has no characterization factor and is therefore not mapped by Ecoinvent. Ecoinvent ID: $elementaryExchangeId"
    )
}

data class FoundMappingExchange(
    override val elementaryExchangeId: String,
    val conversionFactor: Double? = null,
    val name: String? = null,
    val unit: String? = null,
    val compartment: String? = null,
    val subCompartment: String? = null,
    override val comment: String = "",
) : MappingExchange

object EcospoldMethodMapper {
    private val csvFormat: CSVFormat = CSVFormat.Builder.create().setHeader().build()

    /* Flow status, compartment status values:
     * mapped, mapped -> all fields exist (when different from EcoInvent) and should be filled
     *
     * mapped, "" -> sometimes a method_compartment, no method_subcompartment though the substance in our library and
     *               in PEF nomenclature requires it.
     *
     * mapped[,:] overwrite, mapped -> all fields exist (when different from EcoInvent) and should be filled
     *
     * mapped[,:] overwrite, mapped: compartment overwrite -> all fields exist etc...
     *
     * mapped[,:] overwrite, mapped: proxy -> all fields exist etc...
     *
     * mapped[,:] overwrite, "" -> sometimes a method_compartment, no method_subcompartment though the substance in our
     *                             library and in PEF nomenclature requires it.
     */

    fun buildMapping(mapData: Reader): Map<ID, MappingExchange> =
        CSVParser.parse(mapData, csvFormat).use { parser ->
            validateHeaders(parser.headerMap)
            parser.stream().asSequence().mapNotNull { record ->
                when {
                    record["flow_status"] == "ecoinvent orphan" -> {
                        record["id"] to OrphanMappingExchange(record["id"])
                    }

                    record["compartment_status"].isEmpty() -> {
                        record["id"] to UnknownMappingExchange(record["id"])
                    }

                    else -> mappedElement(record)
                }
            }.toMap()
        }

    private fun validateHeaders(headers: Map<String, Int>) =
        sequenceOf(
            "compartment_status",
            "conversion_factor",
            "flow_status",
            "id",
            "method_compartment",
            "method_name",
            "method_subcompartment",
            "method_unit",
            "name",
            "unitName"
        )
            .forEach { header ->
                if (!headers.containsKey(header)) {
                    throw IllegalArgumentException("could not find $header in file headers. Is it a valid mapping file ?")
                }
            }

    private fun mappedElement(record: CSVRecord): Pair<ID, FoundMappingExchange>? =
        try {
            val id = record["id"]
            id to FoundMappingExchange(
                id,
                getConversionFactor(record["conversion_factor"]),
                record["method_name"].nullIfEmpty(),
                record["method_unit"].nullIfEmpty()?.let { pefUnitException(it, record["unitName"]) },
                record["method_compartment"].nullIfEmpty(),
                record["method_subcompartment"].nullIfEmpty(),
                "Ecoinvent ID: $id. Flow, compartment status: ${record["flow_status"]}, ${record["compartment_status"]}",
            )
        } catch (_: IllegalArgumentException) {
            null
        }

    fun getConversionFactor(factor: String): Double? =
        factor
            .nullIfEmpty()
            ?.toDoubleOrNull()
            ?.let {
                if (it == 1.0) null else it
            }

    private fun pefUnitException(methodUnit: String, unitName: String): String =
        if (unitName == "m2*year" && methodUnit == "m2*a") {
            unitName
        } else {
            methodUnit
        }

    private fun String.nullIfEmpty(): String? = this.ifEmpty { null }
}
