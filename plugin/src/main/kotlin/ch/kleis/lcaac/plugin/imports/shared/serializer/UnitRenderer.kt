package ch.kleis.lcaac.plugin.imports.shared.serializer

import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.value.UnitValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.prelude.Prelude
import ch.kleis.lcaac.plugin.imports.ModelWriter
import ch.kleis.lcaac.plugin.imports.model.ImportedUnit
import ch.kleis.lcaac.plugin.imports.util.sanitizeSymbol
import ch.kleis.lcaac.plugin.imports.util.ImportException
import ch.kleis.lcaac.plugin.imports.util.StringUtils.sanitize

class UnitRenderer(
    val knownUnitsByRef: MutableMap<String, UnitValue<BasicNumber>>,
) {
    private val knownUnitsBySymbol = knownUnitsByRef
        .entries.map { it.key to it.value }
        .associateBy { it.second.symbol.toString() }
        .toMutableMap()

    data class AliasFor(val alias: Dimension, val aliasFor: Dimension) {
        constructor(alias: String, aliasFor: Dimension) : this(Dimension.of(alias), aliasFor)
    }

    private val dimAlias = listOf(
        AliasFor("volume", Prelude.volume),
        AliasFor("area", Prelude.area),
        AliasFor("power", Prelude.power),
        AliasFor("amount", Prelude.none),
        AliasFor("land use", Prelude.land_occupation),
        AliasFor("transport", Prelude.transport),
        AliasFor("length.time", Prelude.length_time),
        AliasFor("person.distance", Prelude.person_distance),
        AliasFor("mass.time", Prelude.mass_time),
        AliasFor("volume.time", Prelude.volume_time),
    ).associateBy { it.alias }
    var nbUnit = 0

    companion object {
        fun of(existingUnits: Map<String, UnitValue<BasicNumber>>): UnitRenderer {
            return UnitRenderer(existingUnits.toMutableMap())
        }
    }


    fun render(unit: ImportedUnit, writer: ModelWriter) {
        val dimensionName = unit.dimension.lowercase()
        val dimension = Dimension.of(dimensionName)
        val ref = sanitizeSymbol(sanitize(unit.name, false))
        val existingUnit = getUnit(unit.name)

        when {
            existingUnit != null && areCompatible(existingUnit.second.dimension, dimension) -> {
                /* Nothing to do */
            }

            existingUnit != null && !areCompatible(existingUnit.second.dimension, dimension) ->
                throw ImportException("A Unit ${unit.refUnitName} for ${unit.name} already exists with another dimension, $dimension is not compatible with ${existingUnit.second.dimension}.")

            isNewDimensionReference(dimension, unit.scaleFactor) -> {
                addUnit(ref, UnitValue(UnitSymbol.of(unit.name), 1.0, dimension))
                val block = generateUnitBlockWithNewDimension(ref, unit.name, dimensionName, unit.comment)
                writer.writeAppendFile("unit", block)
            }

            else -> {
                addUnit(ref, UnitValue(UnitSymbol.of(unit.name), unit.scaleFactor, dimension))

                if (unit.refUnitName == ref) {
                    throw ImportException("Unit $ref is referencing itself in its own declaration")
                } else {
                    val block =
                        generateUnitAliasBlock(
                            ref,
                            unit.name,
                            "${unit.scaleFactor} ${unit.refUnitName}",
                            unit.comment
                        )
                    writer.writeAppendFile("unit", block)
                }
            }
        }
        nbUnit++
    }

    private fun generateUnitBlockWithNewDimension(
        symbol: String,
        unitName: String,
        dimensionName: String,
        comment: String?
    ): String {
        val sanitizedComment = if (comment != null) " // $comment" else ""
        return """

        unit $symbol {$sanitizedComment
            symbol = "$unitName"
            dimension = "$dimensionName"
        }
        """.trimIndent()
    }

    private fun generateUnitAliasBlock(symbol: String, unitName: String, alias: String, comment: String?): String {
        val sanitizedComment = if (comment != null) " // $comment" else ""
        return """
    
        unit $symbol {$sanitizedComment
            symbol = "$unitName"
            alias_for = $alias
        }""".trimIndent()
    }

    private fun getUnit(symbol: String): Pair<String, UnitValue<BasicNumber>>? {
        val ref = knownUnitsBySymbol[symbol]?.first ?: return null
        val unit = knownUnitsByRef[ref] ?: return null
        return ref to unit
    }

    private fun addUnit(ref: String, value: UnitValue<BasicNumber>) {
        knownUnitsByRef[ref] = value
        knownUnitsBySymbol[value.symbol.toString()] = ref to value
    }

    private fun isNewDimensionReference(dimension: Dimension, scaleFactor: Double): Boolean {
        val allDimWithReference = knownUnitsByRef.values
            .filter { it.scale == 1.0 }
            .map { it.dimension }
        val isCompatibleWithNoOne = allDimWithReference
            .map { areCompatible(dimension, it) }
            .none { it }
        return scaleFactor == 1.0 && isCompatibleWithNoOne
    }

    fun areCompatible(dim1: Dimension, dim2: Dimension): Boolean {
        return areCompatibleSym(dim1, dim2) || areCompatibleSym(dim2, dim1)
    }

    private fun areCompatibleSym(dim1: Dimension, dim2: Dimension): Boolean {
        return dim1 == dim2 || dimAlias[dim1]?.aliasFor == dim2
    }

}
