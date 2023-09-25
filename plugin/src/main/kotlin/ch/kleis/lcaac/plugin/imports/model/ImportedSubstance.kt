package ch.kleis.lcaac.plugin.imports.model

import ch.kleis.lcaac.plugin.imports.util.StringUtils

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
        uid = pUid?.let { StringUtils.sanitize(it) } ?: StringUtils.sanitize(name)
    }

    fun referenceUnitSymbol() = StringUtils.sanitize(referenceUnit, false)
}

data class ImportedImpact(
        val uid: String,
        val name: String,
        val value: Double,
        val unitSymbol: String,
        val comment: String?
) {
    constructor(value: Double, unitName: String, name: String, comment: String? = null) : this(
            StringUtils.sanitize(name),
            name,
            value,
            StringUtils.sanitize(unitName, toLowerCase = false),
            comment
    )
}
