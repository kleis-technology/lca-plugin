package ch.kleis.lcaac.plugin.imports.ecospold.model

data class ImpactIndicator(
    val methodName: String,
    val categoryName: String,
    val name: String,
    val amount: Double,
    val unitName: String
)
