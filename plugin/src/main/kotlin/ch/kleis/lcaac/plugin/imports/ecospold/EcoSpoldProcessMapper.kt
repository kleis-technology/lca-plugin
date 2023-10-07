package ch.kleis.lcaac.plugin.imports.ecospold

import ch.kleis.lcaac.core.lang.expression.SubstanceType
import ch.kleis.lcaac.plugin.imports.ecospold.model.*
import ch.kleis.lcaac.plugin.imports.model.*
import ch.kleis.lcaac.plugin.imports.util.sanitizeSymbol
import ch.kleis.lcaac.plugin.imports.util.ImportException
import ch.kleis.lcaac.plugin.imports.util.StringUtils.asComment
import ch.kleis.lcaac.plugin.imports.util.StringUtils.compact
import ch.kleis.lcaac.plugin.imports.util.StringUtils.compactList
import ch.kleis.lcaac.plugin.imports.util.StringUtils.merge
import ch.kleis.lcaac.plugin.imports.util.StringUtils.sanitize

object EcoSpoldProcessMapper {
    fun map(
        process: ActivityDataset,
        processDict: Map<String, EcoSpoldImporter.ProcessDictRecord>,
        knownUnits: Set<String>,
        methodName: String? = null,
    ): ImportedProcess {
        val elementaryExchangeGrouping = process.flowData.elementaryExchanges.groupingBy {
            it.substanceType
        }.aggregate { _, accumulator: MutableList<ImportedBioExchange>?, element: ElementaryExchange, _ ->
            val mappedExchange = elementaryExchangeToImportedBioExchange(element, knownUnits)
            accumulator?.let {
                accumulator.add(mappedExchange)
                accumulator
            } ?: mutableListOf(mappedExchange)
        }

        val mappedIntermediateExchanges = process.flowData.intermediateExchanges.map { intermediateExchange ->
            intermediateExchangeToImportedTechnosphereExchange(
                intermediateExchange,
                processDict,
                knownUnits,
            )
        }.groupBy {
            when (it) {
                is ImportedProductExchange -> ImportedProductExchange
                is ImportedInputExchange -> ImportedInputExchange
            }
        }

        val (mappedImpactsComment, mappedImpactsExchanges) = methodName?.let {
            mapImpacts(methodName, process.flowData.impactIndicators, knownUnits)
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

    // sanitize will remove the trailing underscore
    fun buildName(data: ActivityDataset): String {
        return sanitize(data.description.activity.name + "_" + (data.description.geography?.shortName ?: ""))
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

    private fun mapImpacts(
        methodName: String,
        impactIndicatorList: Sequence<ImpactIndicator>,
        knownUnits: Set<String>,
    ): Pair<String, Sequence<ImportedImpactExchange>> =
        Pair("Impacts for method $methodName", impactIndicatorList.filter { it.methodName == methodName }.map {
            ImportedImpactExchange(
                it.amount.toString(),
                unitToStr(it.unitName, knownUnits),
                sanitize(it.name),
                listOf(it.categoryName),
            )
        })

    private fun intermediateExchangeToImportedTechnosphereExchange(
        e: IntermediateExchange,
        processDict: Map<String, EcoSpoldImporter.ProcessDictRecord>,
        knownUnits: Set<String>,
    ): ImportedTechnosphereExchange {
        val name = sanitize(e.name)
        val amount = e.amount.toString()
        val unit = unitToStr(e.unit, knownUnits)
        val comments = buildIntermediateExchangeComments(e)

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
                    comments = comments
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
                            comments = comments,
                        )
                    }

                    else -> throw ImportException("Invalid inputGroup for intermediateExchange, expected in {1, 2, 3, 5}, found ${e.inputGroup}")
                }
            }

            else -> throw ImportException("Intermediate exchange without inputGroup or outputGroup")
        }
    }

    private fun buildIntermediateExchangeComments(
        e: IntermediateExchange,
    ): List<String> {
        val initComments = ArrayList<String>()
        initComments.add(e.name)
        e.classifications.forEach { initComments.add("${it.system} = ${it.value}") }
        e.uncertainty?.let { uncertaintyToStr(initComments, it) }
        e.synonyms.forEachIndexed { i, it -> initComments.add("synonym_$i = $it") }
        e.properties.forEach { initComments.add("${it.name} ${it.amount} ${it.unit} isCalculatedAmount=${it.isCalculatedAmount ?: ""} isDefiningValue=${it.isDefiningValue ?: ""}") }
        return initComments
    }

    private fun elementaryExchangeToImportedBioExchange(elementaryExchange: ElementaryExchange, knownUnits: Set<String>): ImportedBioExchange =
        ImportedBioExchange(
            comments = elementaryExchange.comment?.let { listOf(it) } ?: listOf(),
            qty = elementaryExchange.amount.toString(),
            unit = unitToStr(elementaryExchange.unit, knownUnits),
            name = sanitize(elementaryExchange.name),
            compartment = elementaryExchange.compartment,
            subCompartment = elementaryExchange.subCompartment,
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

    // See issue #348. If re-written version of composite unit is in knownUnits, then use rewritten version; else do not
    // rewrite composition operations (star, etc.) and count on Prelude and arithmetic to build it.
    fun unitToStr(unit: String, knownUnits: Set<String>): String =
        if (knownUnits.contains(unit)) {
            sanitize(sanitizeSymbol(unit), toLowerCase = false)
        } else {
            sanitizeSymbol(unit)
        }

    private fun uncertaintyToStr(comments: ArrayList<String>, it: Uncertainty) {
        it.logNormal?.let { comments.add("// uncertainty: logNormal mean=${it.meanValue}, variance=${it.variance}, mu=${it.mu}") }
        it.pedigreeMatrix?.let { comments.add("// uncertainty: pedigreeMatrix completeness=${it.completeness}, reliability=${it.reliability}, geoCor=${it.geographicalCorrelation}, tempCor=${it.temporalCorrelation}, techCor=${it.furtherTechnologyCorrelation}, ") }
        it.normal?.let { comments.add("// uncertainty: normal mean=${it.meanValue}, variance=${it.variance}, varianceWithPedigreeUncertainty=${it.varianceWithPedigreeUncertainty}, ") }
        it.uniform?.let { comments.add("// uncertainty: uniform minValue=${it.minValue}, maxValue=${it.maxValue}") }
        it.triangular?.let { comments.add("// uncertainty: triangular minValue=${it.minValue}, maxValue=${it.maxValue}, mostLikelyValue=${it.mostLikelyValue}, ") }
        it.comment?.let { comments.add("// uncertainty: comment") }
        it.comment?.let { comments.add(asComment(it)) }
    }
}
