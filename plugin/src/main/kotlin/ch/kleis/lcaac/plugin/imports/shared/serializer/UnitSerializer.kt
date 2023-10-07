package ch.kleis.lcaac.plugin.imports.shared.serializer

import ch.kleis.lcaac.plugin.imports.model.ImportedUnit
import ch.kleis.lcaac.plugin.imports.util.ImportException

class UnitSerializer {
    fun serialize(unit: ImportedUnit): CharSequence {
        return if (unit.isAliasFor()) serializeAliasFor(unit) else serializeLiteral(unit)
    }

    private fun serializeAliasFor(unit: ImportedUnit): CharSequence {
        val sanitizedComment = unit.comment?.let { " // $it" } ?: ""
        val aliasFor = unit.aliasFor ?: throw ImportException("$unit cannot be serialized with an alias")
        return """
                unit ${unit.ref()} {$sanitizedComment
                    symbol = "${unit.symbol}"
                    alias_for = ${aliasFor.scale} ${aliasFor.baseUnitExpressionStr}
                }
                
            """.trimIndent()
    }

    private fun serializeLiteral(unit: ImportedUnit): CharSequence {
        val sanitizedComment = unit.comment?.let { " // $it" } ?: ""
        return """
            unit ${unit.ref()} {$sanitizedComment
                symbol = "${unit.symbol}"
                dimension = "${unit.dimension}"
            }
            
        """.trimIndent()
    }
}
