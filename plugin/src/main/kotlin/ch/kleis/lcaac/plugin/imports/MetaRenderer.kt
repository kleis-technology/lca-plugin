package ch.kleis.lcaac.plugin.imports

import ch.kleis.lcaac.plugin.imports.util.StringUtils
import kotlin.reflect.full.declaredMemberProperties

// TODO Delete ?
class MetaRenderer {

    fun render(o: Any?, prefix: String, metas: MutableMap<String, String>) {
        val realPrefix = if (prefix.isNotBlank()) "${prefix}." else prefix
        when (o) {
            null -> return
            is String -> metas[prefix] = StringUtils.compact(o)
            is Map<*, *> -> o.forEach { (k, v) -> render(v, "$realPrefix$k", metas) }
            is Iterable<*> -> o.forEachIndexed { i, v -> render(v, "$realPrefix${i + 1}", metas) }
            else -> {
                o::class.declaredMemberProperties
                    .forEach { p ->
                        val temp = p.getter.call(o)
                        render(temp, "$realPrefix${p.name}", metas)
                    }
            }
        }
    }
}
