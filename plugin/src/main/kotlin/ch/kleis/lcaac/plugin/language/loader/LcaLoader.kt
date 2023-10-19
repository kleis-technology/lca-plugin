package ch.kleis.lcaac.plugin.language.loader

import ch.kleis.lcaac.core.lang.*
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.math.QuantityOperations
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import ch.kleis.lcaac.plugin.language.psi.type.unit.UnitDefinitionType

class LcaLoader<Q>(
    private val files: Sequence<LcaFile>,
    ops: QuantityOperations<Q>,
) {
    private val mapper = LcaMapper(ops)

    fun load(): SymbolTable<Q> {
        with(mapper) {
            val unitDefinitions = files.flatMap { it.getUnitDefinitions() }
            val processDefinitions = files.flatMap { it.getProcesses() }
            val substanceDefinitions = files.flatMap { it.getSubstances() }

            val dimensions: DimensionRegister = try {
                DimensionRegister(
                        unitDefinitions
                            .filter { it.getType() == UnitDefinitionType.LITERAL }
                            .map {
                                val value = it.dimField!!.getValue()
                                DimensionKey(value) to Dimension.of(value)
                            }
                            .toMap()
                    )
            } catch (e: RegisterException) {
                throw EvaluatorException("Duplicate reference units for dimensions ${e.duplicates}")
            }

            val substanceCharacterizations = try {
                SubstanceCharacterizationRegister(
                        substanceDefinitions
                            .map { Pair(it.buildUniqueKey(), substanceCharacterization(it)) }
                            .toMap()
                    )
            } catch (e: RegisterException) {
                throw EvaluatorException("Duplicate substance ${e.duplicates} defined")
            }

            val globals= try {
                DataRegister(
                        unitDefinitions
                            .filter { it.getType() == UnitDefinitionType.LITERAL }
                            .map { DataKey(it.getDataRef().getUID().name) to (unitLiteral(it)) }
                            .toMap()
                    )
                    .plus(
                        unitDefinitions
                            .filter { it.getType() == UnitDefinitionType.ALIAS }
                            .map { DataKey(it.getDataRef().getUID().name) to (unitAlias(it)) }
                            .asIterable()
                    )
                    .plus(
                        files
                            .flatMap { it.getGlobalAssignments() }
                            .map { DataKey(it.first) to dataExpression(it.second) }
                            .asIterable()
                    )
            } catch (e: RegisterException) {
                throw EvaluatorException("Duplicate global variable ${e.duplicates} defined")
            }

            val processTemplates = try {
                ProcessTemplateRegister(
                        processDefinitions
                            .map { Pair(it.buildUniqueKey(), process(it, globals)) }
                            .toMap()
                    )
            } catch (e: RegisterException) {
                throw EvaluatorException("Duplicate process ${e.duplicates} defined")
            }

            return SymbolTable(
                data = globals,
                processTemplates = processTemplates,
                dimensions = dimensions,
                substanceCharacterizations = substanceCharacterizations,
            )
        }
    }

}
