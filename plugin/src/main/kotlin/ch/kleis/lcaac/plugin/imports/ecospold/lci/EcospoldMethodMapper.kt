package ch.kleis.lcaac.plugin.imports.ecospold.lci

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.Reader
import kotlin.reflect.full.memberProperties
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

data class MethodMappingHeaders(
    val flowIdHeader: String,
    val flowNameHeader: String,
    val flowUnitNameHeader: String,
    val flowFlowStatusHeader: String,
    val methodNameHeader: String,
    val methodUnitHeader: String,
    val methodCompartmentHeader: String,
    val methodSubCompartmentHeader: String,
    val conversionFactorHeader: String,
    val compartmentStatusHeader: String,
) {
    companion object Versions {
        val ecoInvent39 = MethodMappingHeaders(
            flowIdHeader = "id",
            flowNameHeader = "name",
            flowUnitNameHeader = "unitName",
            flowFlowStatusHeader = "flow_status",
            methodNameHeader = "method_name",
            methodUnitHeader = "method_unit",
            methodCompartmentHeader = "method_compartment",
            methodSubCompartmentHeader = "method_subcompartment",
            conversionFactorHeader = "conversion_factor",
            compartmentStatusHeader = "compartment_status",
        )

        val ecoInvent310 = MethodMappingHeaders(
            flowIdHeader = "elementary_flow_id",
            flowNameHeader = "elementary_flow_name",
            flowUnitNameHeader = "unit_name",
            flowFlowStatusHeader = "flow_status",
            methodNameHeader = "method_elementary_flow_name",
            methodUnitHeader = "method_unit",
            methodCompartmentHeader = "method_compartment",
            methodSubCompartmentHeader = "method_subcompartment",
            conversionFactorHeader = "conversion_factor",
            compartmentStatusHeader = "compartment_status",
        )
    }
}


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
            val mappingHeaders = validateHeaders(parser.headerMap)
            parser.stream().asSequence().mapNotNull { record ->
                when {
                    record[mappingHeaders.flowFlowStatusHeader] == "ecoinvent orphan" -> {
                        record[mappingHeaders.flowIdHeader] to OrphanMappingExchange(record[mappingHeaders.flowIdHeader])
                    }

                    record[mappingHeaders.compartmentStatusHeader].isEmpty() -> {
                        record[mappingHeaders.flowIdHeader] to UnknownMappingExchange(record[mappingHeaders.flowIdHeader])
                    }

                    else -> mappedElement(mappingHeaders, record)
                }
            }.toMap()
        }

    fun validateHeaders(headers: Map<String, Int>): MethodMappingHeaders =
        when {
            MethodMappingHeaders.ecoInvent39::class.memberProperties.map {
                it.getter.call(MethodMappingHeaders.ecoInvent39).toString()
            }.all { header -> headers.containsKey(header) } -> MethodMappingHeaders.ecoInvent39

            MethodMappingHeaders.ecoInvent310::class.memberProperties.map {
                it.getter.call(MethodMappingHeaders.ecoInvent310).toString()
            }.all { header -> headers.containsKey(header) } -> MethodMappingHeaders.ecoInvent310

            else -> throw IllegalArgumentException("Method mapping file could not be matched to the EcoInvent 3.9.1 or 3.10 header schema. Is it a valid mapping file ?")

        }

    private fun mappedElement(headers: MethodMappingHeaders, record: CSVRecord): Pair<ID, FoundMappingExchange>? =
        try {
            val id = record[headers.flowIdHeader]
            id to FoundMappingExchange(
                id,
                getConversionFactor(record[headers.conversionFactorHeader]),
                record[headers.methodNameHeader].nullIfEmpty(),
                record[headers.methodUnitHeader].nullIfEmpty()?.let { pefUnitException(it, record[headers.flowUnitNameHeader]) },
                record[headers.methodCompartmentHeader].nullIfEmpty(),
                record[headers.methodSubCompartmentHeader].nullIfEmpty(),
                "Ecoinvent ID: $id. Flow, compartment status: ${record[headers.flowFlowStatusHeader]}, ${record[headers.compartmentStatusHeader]}",
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
