package ch.kleis.lcaac.plugin.imports.model

import ch.kleis.lcaac.plugin.imports.util.StringUtils.sanitize
import ch.kleis.lcaac.plugin.imports.util.sanitizeSymbol


class ImportedUnit(
    val dimension: String,
    val symbol: String,
    aliasFor: ImportedUnitAliasFor? = null,
    val comment: String? = null
) {
    val aliasFor = if (aliasFor?.baseUnitExpressionStr == symbol) null else aliasFor

    fun ref(): String {
        return sanitize(sanitizeSymbol(symbol), toLowerCase = false)
    }

    fun isAliasFor(): Boolean {
        return aliasFor != null
    }
}

class ImportedUnitAliasFor(
    val scale: Double,
    baseUnitExpressionStr: String, // e.g., kg/l, m2*year
) {
    val baseUnitExpressionStr = when(baseUnitExpressionStr) {
        "metric ton*km" -> "ton*km"
        else -> baseUnitExpressionStr
    }
}
