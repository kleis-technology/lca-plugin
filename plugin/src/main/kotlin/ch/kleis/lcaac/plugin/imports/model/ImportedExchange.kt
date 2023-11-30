package ch.kleis.lcaac.plugin.imports.model

sealed interface ImportedExchange {
    val qty: String
    val unit: String
    val name: String
    val comments: List<String>
    val printAsComment: Boolean
}

sealed interface ImportedTechnosphereExchange

data class ImportedBioExchange(
    override val qty: String,
    override val unit: String,
    override val name: String,
    val compartment: String,
    var subCompartment: String? = null,
    override val comments: List<String>,
    override val printAsComment: Boolean = false,
) : ImportedExchange

data class ImportedInputExchange(
    val id: String? = null,
    override val name: String,
    override val qty: String,
    override val unit: String,
    val fromProcess: String? = null,
    override val comments: List<String> = emptyList(),
    override val printAsComment: Boolean = false,
) : ImportedTechnosphereExchange, ImportedExchange {
    companion object
}

data class ImportedProductExchange(
    val id: String? = null,
    override val name: String,
    override val qty: String,
    override val unit: String,
    val allocation: Double = 100.0,
    override val comments: List<String> = emptyList(),
    override val printAsComment: Boolean = false,
) : ImportedTechnosphereExchange, ImportedExchange {
    companion object

    fun asInput(): ImportedInputExchange =
        ImportedInputExchange(id = id, name = name, qty = qty, unit = unit, comments = comments)
}

data class ImportedImpactExchange(
    override val qty: String,
    override val unit: String,
    override val name: String,
    override val comments: List<String>,
    override val printAsComment: Boolean = false,
) : ImportedExchange
