package ch.kleis.lcaac.plugin.language.loader

import ch.kleis.lcaac.core.lang.Register
import ch.kleis.lcaac.core.lang.RegisterException
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.EProcessTemplate
import ch.kleis.lcaac.core.lang.expression.ESubstanceCharacterization
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

            val dimensions: Register<Dimension> = try {
                Register.empty<Dimension>()
                    .plus(
                        unitDefinitions
                            .filter { it.getType() == UnitDefinitionType.LITERAL }
                            .map {
                                val value = it.dimField!!.getValue()
                                value to Dimension.of(value)
                            }
                            .asIterable()
                    )
            } catch (e: RegisterException) {
                throw EvaluatorException("Duplicate reference units for dimensions ${e.duplicates}")
            }

            val substanceCharacterizations = try {
                Register.empty<ESubstanceCharacterization<Q>>()
                    .plus(
                        substanceDefinitions
                            .map { Pair(it.buildUniqueKey(), substanceCharacterization(it)) }
                            .asIterable()
                    )
            } catch (e: RegisterException) {
                throw EvaluatorException("Duplicate substance ${e.duplicates} defined")
            }

            val globals: Register<DataExpression<Q>> = try {
                Register.empty<DataExpression<Q>>()
                    .plus(
                        unitDefinitions
                            .filter { it.getType() == UnitDefinitionType.LITERAL }
                            .map { it.getDataRef().getUID().name to (unitLiteral(it)) }
                            .asIterable()
                    )
                    .plus(
                        unitDefinitions
                            .filter { it.getType() == UnitDefinitionType.ALIAS }
                            .map { it.getDataRef().getUID().name to (unitAlias(it)) }
                            .asIterable()
                    )
                    .plus(
                        files
                            .flatMap { it.getGlobalAssignments() }
                            .map { it.first to dataExpression(it.second) }
                            .asIterable()
                    )
            } catch (e: RegisterException) {
                throw EvaluatorException("Duplicate global variable ${e.duplicates} defined")
            }

            val processTemplates = try {
                Register.empty<EProcessTemplate<Q>>()
                    .plus(
                        processDefinitions
                            .map { Pair(it.buildUniqueKey(), process(it, globals)) }
                            .asIterable()
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
