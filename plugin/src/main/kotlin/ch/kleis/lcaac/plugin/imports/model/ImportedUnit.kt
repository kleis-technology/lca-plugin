package ch.kleis.lcaac.plugin.imports.model

data class ImportedUnit(
    val dimension: String,
    val name: String,
    val scaleFactor: Double,
    val refUnitName: String,
    val comment: String? = null
)
