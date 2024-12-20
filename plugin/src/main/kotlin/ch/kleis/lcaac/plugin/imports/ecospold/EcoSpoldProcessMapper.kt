package ch.kleis.lcaac.plugin.imports.ecospold

import ch.kleis.lcaac.core.lang.expression.SubstanceType
import ch.kleis.lcaac.core.prelude.Prelude.Companion.sanitize
import ch.kleis.lcaac.plugin.imports.ecospold.model.*
import ch.kleis.lcaac.plugin.imports.model.*
import ch.kleis.lcaac.plugin.imports.shared.UnitManager
import ch.kleis.lcaac.plugin.imports.util.ImportException
import ch.kleis.lcaac.plugin.imports.util.StringUtils.asComment
import ch.kleis.lcaac.plugin.imports.util.StringUtils.compact
import ch.kleis.lcaac.plugin.imports.util.StringUtils.compactList
import ch.kleis.lcaac.plugin.imports.util.StringUtils.merge
import ch.kleis.lcaac.plugin.imports.util.sanitizeSymbol

class EcoSpoldProcessMapper(
    private val processDict: Map<String, EcoSpoldImporter.ProcessDictRecord>,
    private val unitManager: UnitManager,
    private val methodName: String? = null,
) {
    companion object {
        // sanitize will remove the trailing underscore
        fun buildName(data: ActivityDataset): String {
            return sanitize(data.description.activity.name + "_" + (data.description.geography?.shortName ?: ""))
        }
    }

    fun map(
        process: ActivityDataset,
    ): ImportedProcess {
        val elementaryExchangeGrouping = process.flowData.elementaryExchanges.groupingBy {
            it.substanceType
        }.aggregate { _, accumulator: MutableList<ImportedBioExchange>?, element: ElementaryExchange, _ ->
            val mappedExchange = elementaryExchangeToImportedBioExchange(element)
            accumulator?.let {
                accumulator.add(mappedExchange)
                accumulator
            } ?: mutableListOf(mappedExchange)
        }

        val mappedIntermediateExchanges = process.flowData.intermediateExchanges.map { intermediateExchange ->
            intermediateExchangeToImportedTechnosphereExchange(
                intermediateExchange,
                processDict,
            )
        }.groupBy {
            when (it) {
                is ImportedProductExchange -> ImportedProductExchange
                is ImportedInputExchange -> ImportedInputExchange
            }
        }

        val (mappedImpactsComment, mappedImpactsExchanges) = methodName?.let {
            mapImpactExchanges(methodName, process.flowData.impactExchanges)
        } ?: (null to emptySequence())

        return ImportedProcess(
            uid = buildName(process),
            meta = mapMetas(process.description),
            // used for EcoInvent 3.9.1 coproduct import
            labels = mapLabels(mappedIntermediateExchanges[ImportedProductExchange]),
            productBlocks = listOfNonEmptyExchangeBlock(
                mappedIntermediateExchanges[ImportedProductExchange]
                    ?.asSequence()
                    ?.map { it as ImportedProductExchange }
            ),
            inputBlocks = listOfNonEmptyExchangeBlock(
                mappedIntermediateExchanges[ImportedInputExchange]
                    ?.asSequence()
                    ?.map { it as ImportedInputExchange }
            ),
            emissionBlocks = listOfNonEmptyExchangeBlock(
                elementaryExchangeGrouping[SubstanceType.EMISSION]?.asSequence()
            ),
            resourceBlocks = listOfNonEmptyExchangeBlock(
                elementaryExchangeGrouping[SubstanceType.RESOURCE]?.asSequence()
            ),
            landUseBlocks = listOfNonEmptyExchangeBlock(
                elementaryExchangeGrouping[SubstanceType.LAND_USE]?.asSequence()
            ),
            impactBlocks = listOfNonEmptyExchangeBlock(
                mappedImpactsExchanges, mappedImpactsComment
            ),
        )
    }

    private fun mapLabels(productExchanges: Iterable<ImportedTechnosphereExchange>?): Map<String, String?> =
        productExchanges?.firstNotNullOfOrNull {
            (it as? ImportedProductExchange)?.name
        }?.let {
            mapOf("productName" to sanitize(it))
        } ?: emptyMap()

    private fun mapMetas(description: ActivityDescription): Map<String, String?> =
        mapOf("id" to description.activity.id?.let { compact(it) },
            "name" to description.activity.name.let { compact(it) },
            "type" to description.activity.type,
            "description" to description.activity.generalComment?.let { merge(compactList(it)) },
            "energyValues" to description.activity.energyValues,
            "includedActivitiesStart" to description.activity.includedActivitiesStart?.let { compact(it) },
            "includedActivitiesEnd" to description.activity.includedActivitiesEnd?.let { compact(it) },
            "geography-shortname" to description.geography?.shortName?.let { compact(it) },
            "geography-comment" to description.geography?.comment?.let { merge(compactList(it)) }
        ) + description.classifications.associate {
            it.system to compact(it.value)
        }

    private fun mapImpactExchanges(
        methodName: String,
        impactIndicatorList: Sequence<ImpactExchange>,
    ): Pair<String, Sequence<ImportedImpactExchange>> =
        Pair("Impacts for method $methodName", impactIndicatorList.filter { it.indicator.methodName == methodName }.map {
            ImportedImpactExchange(
                it.amount.toString(),
                unitManager.findRefBySymbolOrSanitizeSymbol(it.indicator.unitName),
                sanitizeSymbol(sanitize(it.indicator.categoryName)),
                listOf(it.indicator.name),
            )
        })

    private fun intermediateExchangeToImportedTechnosphereExchange(
        e: IntermediateExchange,
        processDict: Map<String, EcoSpoldImporter.ProcessDictRecord>,
    ): ImportedTechnosphereExchange {
        val name = sanitize(e.name)
        val amount = e.amount.toString()
        val unit = unitManager.findRefBySymbolOrSanitizeSymbol(e.unit)

        when {
            e.outputGroup != null -> {
                if (e.outputGroup != 0) {
                    throw ImportException("Invalid outputGroup for product, expected 0, found ${e.outputGroup}")
                }

                return ImportedProductExchange(
                    id = e.id,
                    name = name,
                    qty = amount,
                    unit = unit,
                    comments = buildProductExchangeComments(e)
                )
            }

            e.inputGroup != null -> {
                when (e.inputGroup) {
                    1, 2, 3, 5 -> {
                        val fromProcess = e.activityLinkId?.let {
                            processDict[e.activityLinkId]
                        }
                        val fromProcessName = fromProcess?.let {
                            sanitize("${it.processName}_${it.geo}")
                        }
                        val fromProcessLabels = e.id?.let { " match (productName = \"${sanitize(e.name)}\")" } ?: ""
                        return ImportedInputExchange(
                            id = e.id,
                            name = name,
                            qty = amount,
                            unit = unit,
                            fromProcess = "$fromProcessName$fromProcessLabels",
                        )
                    }

                    else -> throw ImportException("Invalid inputGroup for intermediateExchange, expected in {1, 2, 3, 5}, found ${e.inputGroup}")
                }
            }

            else -> throw ImportException("Intermediate exchange without inputGroup or outputGroup")
        }
    }

    private fun buildProductExchangeComments(
        e: IntermediateExchange,
    ): List<String> {
        val initComments = mutableListOf(e.name)
        e.classifications.forEach { initComments.add("${it.system} = ${it.value}") }
        e.uncertainty?.let { uncertaintyToStr(initComments, it) }
        e.synonyms.forEachIndexed { i, it -> initComments.add("synonym_$i = $it") }
        e.properties.forEach { initComments.add("${it.name} ${it.amount} ${it.unit} isCalculatedAmount=${it.isCalculatedAmount ?: ""} isDefiningValue=${it.isDefiningValue ?: ""}") }
        return initComments
    }

    private fun elementaryExchangeToImportedBioExchange(elementaryExchange: ElementaryExchange): ImportedBioExchange =
        ImportedBioExchange(
            qty = elementaryExchange.amount.toString(),
            unit = unitManager.findRefBySymbolOrSanitizeSymbol(elementaryExchange.unit),
            name = sanitize(elementaryExchange.name),
            compartment = elementaryExchange.compartment,
            subCompartment = elementaryExchange.subCompartment,
            comments = elementaryExchange.comment?.let { listOf(it) } ?: listOf(),
            printAsComment = elementaryExchange.printAsComment,
        )

    private fun <E : ImportedExchange> listOfNonEmptyExchangeBlock(
        exchanges: Sequence<E>?,
        comment: String? = null,
    ): List<ExchangeBlock<E>> =
        // Testing if a sequence is empty is weird. count() will collect it, and there is no isEmpty ...
        exchanges?.firstOrNull()?.let {
            listOf(
                ExchangeBlock(
                    comment = comment, exchanges = exchanges
                )
            )
        } ?: listOf()

    private fun uncertaintyToStr(comments: MutableList<String>, it: Uncertainty) {
        it.logNormal?.let { comments.add("// uncertainty: logNormal mean=${it.meanValue}, variance=${it.variance}, mu=${it.mu}") }
        it.pedigreeMatrix?.let { comments.add("// uncertainty: pedigreeMatrix completeness=${it.completeness}, reliability=${it.reliability}, geoCor=${it.geographicalCorrelation}, tempCor=${it.temporalCorrelation}, techCor=${it.furtherTechnologyCorrelation}, ") }
        it.normal?.let { comments.add("// uncertainty: normal mean=${it.meanValue}, variance=${it.variance}, varianceWithPedigreeUncertainty=${it.varianceWithPedigreeUncertainty}, ") }
        it.uniform?.let { comments.add("// uncertainty: uniform minValue=${it.minValue}, maxValue=${it.maxValue}") }
        it.triangular?.let { comments.add("// uncertainty: triangular minValue=${it.minValue}, maxValue=${it.maxValue}, mostLikelyValue=${it.mostLikelyValue}, ") }
        it.comment?.let { comments.add("// uncertainty: comment") }
        it.comment?.let { comments.add(asComment(it)) }
    }
}
