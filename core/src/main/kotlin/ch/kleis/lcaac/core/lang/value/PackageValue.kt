package ch.kleis.lcaac.core.lang.value

import ch.kleis.lcaac.core.lang.expression.EPackage

data class PackageValue<Q>(
    val name: String,
    val arguments: Map<String, DataValue<Q>> = emptyMap(),
    val with: Map<String, ProductValue<Q>> = emptyMap(),
) {
    companion object {
        fun <Q> default(): PackageValue<Q> = PackageValue(EPackage.DEFAULT_PKG_NAME)
    }
}

