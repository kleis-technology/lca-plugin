package ch.kleis.lcaac.plugin.imports.ecospold.model


data class FlowData(
    val intermediateExchanges: Sequence<IntermediateExchange> = emptySequence(),
    val impactExchanges: Sequence<ImpactExchange> = emptySequence(),
    val elementaryExchanges: Sequence<ElementaryExchange> = emptySequence(),
)
