package ch.kleis.lcaac.plugin.imports.model

import ch.kleis.lcaac.plugin.imports.ModelWriter

class ImportedSubstance(
        val name: String,
        val type: String,
        val referenceUnit: String,
        val compartment: String,
        var subCompartment: String? = null,
        val impacts: MutableList<ImportedImpact> = mutableListOf(),
        val meta: MutableMap<String, String?> = mutableMapOf(),
        pUid: String? = null
) {
    val uid: String

    init {
        uid = pUid?.let { ModelWriter.sanitizeAndCompact(it) } ?: ModelWriter.sanitizeAndCompact(name)
    }

    fun referenceUnitSymbol() = ModelWriter.sanitizeAndCompact(referenceUnit, false)
}

data class ImportedImpact(
        val uid: String,
        val name: String,
        val value: Double,
        val unitSymbol: String,
        val comment: String?
) {
    constructor(value: Double, unitName: String, name: String, comment: String? = null) : this(
            ModelWriter.sanitizeAndCompact(name),
            name,
            value,
            ModelWriter.sanitizeAndCompact(unitName, toLowerCase = false),
            comment
    )
}