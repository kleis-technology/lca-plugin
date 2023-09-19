package ch.kleis.lcaac.plugin.imports.ecospold.model

import ch.kleis.lcaac.core.lang.expression.SubstanceType

data class ElementaryExchange(
    val elementaryExchangeId: String,
    val amount: Double,
    val name: String,
    val unit: String,
    val compartment: String,
    val subCompartment: String?,
    val substanceType: SubstanceType,
    val comment: String?,
    val printAsComment: Boolean = false,
)
