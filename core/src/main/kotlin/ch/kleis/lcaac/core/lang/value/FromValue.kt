package ch.kleis.lcaac.core.lang.value

sealed interface FromValue<Q>

data class FromPackageValue<Q>(
    val pkg: PackageValue<Q>
): FromValue<Q>

data class FromProcessValue<Q>(
    val name: String,
    val matchLabels: Map<String, StringValue<Q>> = emptyMap(),
    val arguments: Map<String, DataValue<Q>> = emptyMap(),
    val pkg: PackageValue<Q>,
): FromValue<Q>
