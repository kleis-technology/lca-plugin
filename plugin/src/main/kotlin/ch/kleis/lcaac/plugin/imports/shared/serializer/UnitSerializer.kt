package ch.kleis.lcaac.plugin.imports.shared.serializer

import ch.kleis.lcaac.plugin.imports.model.ImportedUnit

class UnitSerializer {
    // TODO: Test me
    fun serialize(unit: ImportedUnit): CharSequence {
        val sanitizedComment = unit.comment?.let { " // $it" } ?: ""
        return unit.aliasFor?.let {aliasFor ->
            """
                unit ${unit.ref()} {$sanitizedComment
                    symbol = "${unit.symbol}"
                    alias_for = ${aliasFor.scale} ${aliasFor.baseUnitExpressionStr}
            """.trimIndent()
        } ?: """
            unit ${unit.ref()} {$sanitizedComment
                symbol = "${unit.symbol}"
                dimension = "${unit.dimension}"
            }
        """.trimIndent()
    }
}
