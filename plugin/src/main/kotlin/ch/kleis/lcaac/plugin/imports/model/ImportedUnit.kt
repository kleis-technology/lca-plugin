package ch.kleis.lcaac.plugin.imports.model

import ch.kleis.lcaac.plugin.imports.util.StringUtils.sanitize
import ch.kleis.lcaac.plugin.imports.util.sanitizeSymbol


data class ImportedUnit(
    val dimension: String,
    val symbol: String,
    val aliasFor: ImportedUnitAliasFor? = null,
    val comment: String? = null
) {
    fun ref(): String {
        return refOf(symbol)
    }

    companion object {
        fun refOf(symbol: String): String {
            return sanitize(sanitizeSymbol(symbol), toLowerCase = false)
        }
    }
}

data class ImportedUnitAliasFor(
    val scale: Double,
    val baseUnitExpressionStr: String, // e.g., kg/l, m2*year
)
