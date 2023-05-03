package ch.kleis.lcaplugin.core.lang

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.expression.*

data class SymbolTable(
        val quantities: Register<QuantityExpression> = Register.empty(),
        val units: Register<UnitExpression> = Register.empty(),
        val processTemplates: Register<EProcessTemplate> = Register.empty(),
        val substanceCharacterizations: Register<ESubstanceCharacterization> = Register.empty(),
) {
    private val templatesIndexedByProductName: Index<String, EProcessTemplate> = Index(
            processTemplates,
            EProcessTemplate.body.products compose
                    Every.list() compose
                    ETechnoExchange.product compose
                    EProductSpec.name
    )

    private val substanceCharacterizationsIndexed = buildSubstanceIndex(substanceCharacterizations)

    private fun buildSubstanceIndex(substanceCharacterizations: Register<ESubstanceCharacterization>): Map<Triple<String, SubstanceType, String>, Map<String?, ESubstanceCharacterization>> {
        fun getName(esc: ESubstanceCharacterization): String = esc.referenceExchange.substance.name
        fun getType(esc: ESubstanceCharacterization): SubstanceType? = esc.referenceExchange.substance.type
        fun getCompartment(esc: ESubstanceCharacterization): String? = esc.referenceExchange.substance.compartment
        fun getSubCompartment(esc: ESubstanceCharacterization): String? = esc.referenceExchange.substance.subCompartment

        return substanceCharacterizations.getValues()
                .groupBy { Triple(getName(it), getType(it)!!, getCompartment((it))!!) }.mapValues { mapEntry ->
                    mapEntry.value.associateBy(::getSubCompartment)
                }
    }

    companion object {
        fun empty() = SymbolTable()
    }

    fun getTemplate(name: String): EProcessTemplate? {
        return processTemplates[name]
    }

    fun getUnit(name: String): UnitExpression? {
        return units[name]
    }

    fun getQuantity(name: String): QuantityExpression? {
        return quantities[name]
    }

    @Deprecated("Use version with name, type, compartment, subCompartment? instead", ReplaceWith("getSubstanceCharacterization(name: String, type: SubstanceType, compartment: String)"))
    fun getSubstanceCharacterization(name: String): ESubstanceCharacterization? {
        return substanceCharacterizations[name]
    }

    fun getSubstanceCharacterization(name: String, type: SubstanceType, compartment: String): ESubstanceCharacterization? =
            substanceCharacterizationsIndexed[Triple(name, type, compartment)]?.get(null)

    fun getSubstanceCharacterization(name: String, type: SubstanceType, compartment: String, subCompartment: String): ESubstanceCharacterization? =
            substanceCharacterizationsIndexed[Triple(name, type, compartment)]?.get(subCompartment)

    fun getTemplateFromProductName(name: String): EProcessTemplate? {
        return templatesIndexedByProductName[name]
    }

    override fun toString(): String {
        return "[symbolTable]"
    }
}

