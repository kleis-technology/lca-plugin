package ch.kleis.lcaac.plugin.imports.ecospold.model


data class Property(
    val name: String,
    val amount: Double,
    val unit: String,
    val isDefiningValue: String?,
    val isCalculatedAmount: String?
)

data class IntermediateExchange(
    val id: String? = null,
    val name: String,
    val amount: Double,
    val unit: String,
    val synonyms: List<String> = emptyList(),
    val uncertainty: Uncertainty? = null,
    val outputGroup: Int? = null,
    val inputGroup: Int? = null,
    val activityLinkId: String? = null,
    val classifications: List<Classification> = emptyList(),
    val properties: List<Property> = emptyList(),
)
