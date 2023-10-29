package ch.kleis.lcaac.core.lang.expression

import arrow.optics.optics

@optics
sealed interface FromExpression<Q> {
    companion object
}

@optics
data class FromPackage<Q>(
    val pkg: PackageImportExpression<Q>
) : FromExpression<Q> {
    override fun toString(): String {
        return "from $pkg"
    }
    companion object {}
}

@optics
data class FromProcess<Q>(
    val name: String,
    val matchLabels: MatchLabels<Q> = MatchLabels(emptyMap()),
    val arguments: Map<String, DataExpression<Q>> = emptyMap(),
    val pkg: PackageImportExpression<Q>? = null,
) : FromExpression<Q> {
    override fun toString(): String {
        return if (pkg != null)  "from $pkg.$name$matchLabels$arguments"
        else "from $name$matchLabels$arguments"
    }

    companion object
}

@optics
data class MatchLabels<Q>(
    val elements: Map<String, DataExpression<Q>>,
) {
    override fun toString(): String {
        return if (elements.isEmpty()) "" else "$elements"
    }

    companion object
}
