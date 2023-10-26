package ch.kleis.lcaac.core.lang.expression

import arrow.optics.Every
import ch.kleis.lcaac.core.lang.Index
import ch.kleis.lcaac.core.lang.register.*

sealed interface PackageExpression<Q>

data class EPackage<Q>(
    val name: String,
    val params: DataRegister<Q> = DataRegister.empty(),
    val data: DataRegister<Q> = DataRegister.empty(),
    val dimensions: DimensionRegister = DimensionRegister.empty(),
    val processTemplates: ProcessTemplateRegister<Q> = ProcessTemplateRegister.empty(),
    val substanceCharacterizations: SubstanceCharacterizationRegister<Q> = SubstanceCharacterizationRegister.empty(),
    val imports: ImportRegister<Q> = ImportRegister.empty(),
) : PackageExpression<Q> {

    companion object {
        private const val DEFAULT_PKG_NAME = "default"
        fun <Q> empty() = EPackage<Q>(name = DEFAULT_PKG_NAME)
    }

    override fun toString(): String {
        return "[symbolTable]"
    }

    /*
        Templates
     */

    private val templatesIndexedByProductName: Index<String, ProcessKey, EProcessTemplate<Q>> = Index(
        processTemplates,
        EProcessTemplate.body<Q>().products() compose
            Every.list() compose
            ETechnoExchange.product() compose
            EProductSpec.name()
    )

    fun getTemplate(name: String): EProcessTemplate<Q>? {
        return processTemplates[ProcessKey(name)]
    }

    fun getTemplate(name: String, labels: Map<String, String>): EProcessTemplate<Q>? {
        return processTemplates[ProcessKey(name, labels)]
    }

    fun getAllTemplatesByProductName(name: String): List<EProcessTemplate<Q>> {
        return templatesIndexedByProductName.getAll(name)
    }


    /*
        Substances
     */
    fun getSubstanceCharacterization(
        name: String,
        type: SubstanceType,
        compartment: String,
        subCompartment: String? = null,
    ): ESubstanceCharacterization<Q>? {
        return substanceCharacterizations[SubstanceKey(name, type, compartment, subCompartment)]
    }

    /*
        Data
     */
    fun getData(name: String): DataExpression<Q>? {
        return data[DataKey(name)]
    }
}

data class EImport<Q>(
    val name: String,
    val arguments: Map<String, DataExpression<Q>> = emptyMap(),
    val with: Map<String, EProductSpec<Q>> = emptyMap(),
) : PackageExpression<Q>

data class EImportRef<Q>(
    val name: String,
) : PackageExpression<Q>
