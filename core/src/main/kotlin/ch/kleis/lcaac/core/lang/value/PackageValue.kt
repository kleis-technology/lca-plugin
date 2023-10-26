package ch.kleis.lcaac.core.lang.value

data class PackageValue<Q>(
    val name: String,
    val arguments: Map<String, DataValue<Q>> = emptyMap(),
    val with: Map<String, ProductValue<Q>> = emptyMap(),
)

