package ch.kleis.lcaac.plugin.imports.simapro

import ch.kleis.lcaac.plugin.language.reservedWords

fun sanitizeSymbol(symbol: String): String {
    return when (symbol) {
        "metric ton*km" -> "ton*km"
        "unit" -> "u"
        in reservedWords -> "_$symbol"
        else -> symbol
    }
}
