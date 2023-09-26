package ch.kleis.lcaac.plugin.imports.util

import com.intellij.util.applyIf

object StringUtils {
    const val BASE_PAD = 4

    fun sanitize(s: String, toLowerCase: Boolean = true): String {
        if (s.isBlank()) {
            return s
        }

        val r = if (s[0].isDigit()) "_$s" else s
        val spaces = """\s+""".toRegex()
        val nonAlphaNumeric = """[^a-zA-Z0-9_]+""".toRegex()
        val underscores = Regex("_{2,}")

        return r.applyIf(toLowerCase, String::lowercase).trim()
            .replace(spaces, "_")
            .replace("*", "_m_")
            .replace("+", "_p_")
            .replace("&", "_a_")
            .replace(">", "_gt_")
            .replace("<", "_lt_")
            .replace("/", "_sl_")
            .replace(nonAlphaNumeric, "_")
            .replace(underscores, "_")
            .trimEnd('_')
    }

    fun merge(s: Collection<CharSequence>): String = s.joinToString("\n")

    fun compact(s: CharSequence): String =
        s.ifBlank { null }
            ?.replace(Regex.fromLiteral("\""), "'")
            ?.let(::trimTrailingNonPrinting)
            ?.toString()
            ?: ""

    fun compactList(s: Collection<CharSequence>): Collection<String> =
        s.filter(CharSequence::isNotBlank).map(this::compact)

    fun asComment(str: CharSequence): String = str.lines().joinToString("\n") { "// $it" }

    fun asCommentList(str: Collection<CharSequence>): Collection<String> = str.map(::asComment)

    fun trimTrailingNonPrinting(s: CharSequence): CharSequence = s.trimEnd('\n').trimEnd()

    fun formatMetaValues(metas: Map<String, String?>): CharSequence {
        val builder = StringBuilder()
        metas.mapNotNull { (k, v) ->
            v?.lines()?.map(::compact)?.let { lines ->

                val metaHead = lines.take(1)
                val metaTail = lines.drop(1).filter(String::isNotBlank).map(String::prependIndent)

                "\"$k\" = \"${merge(metaHead + metaTail)}\""
            }
        }.joinTo(builder, "\n")
        return builder
    }

    fun formatLabelValues(labels: Map<String, String?>): CharSequence {
        val builder = StringBuilder()
        labels.mapNotNull { (k, v) -> v?.let {
            "$k = \"$v\""
        }}.joinTo(builder, "\n")
        return builder
    }

}