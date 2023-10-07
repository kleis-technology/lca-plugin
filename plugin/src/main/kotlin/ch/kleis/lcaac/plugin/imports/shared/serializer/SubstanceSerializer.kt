package ch.kleis.lcaac.plugin.imports.shared.serializer

import ch.kleis.lcaac.plugin.imports.model.ImportedImpact
import ch.kleis.lcaac.plugin.imports.model.ImportedSubstance
import ch.kleis.lcaac.plugin.imports.util.sanitizeSymbol
import ch.kleis.lcaac.plugin.imports.util.StringUtils.asComment
import ch.kleis.lcaac.plugin.imports.util.StringUtils.formatMetaValues
import ch.kleis.lcaac.plugin.imports.util.StringUtils.sanitize

class SubstanceSerializer {

    companion object {

        fun serialize(s: ImportedSubstance): CharSequence {

            val builder = StringBuilder()

            // Header
            builder.append("substance ${s.uid} {")
            builder.appendLine().appendLine()

            // Body
            builder.append(
            """name = "${s.name}"
              |type = ${s.type}
              |compartment = "${s.compartment}"
            """.trimMargin().prependIndent())
            builder.appendLine()

            if (!s.subCompartment.isNullOrBlank()) {
                builder.append("""sub_compartment = "${s.subCompartment}"""".prependIndent())
                builder.appendLine()
            }

            builder.append("reference_unit = ${s.referenceUnitSymbol()}".prependIndent())
            builder.appendLine().appendLine()

            // Meta
            val metaValues = formatMetaValues(s.meta).toString().prependIndent()
            val metaBlock = "meta {\n$metaValues\n}".prependIndent()

            builder.append(metaBlock)
            builder.appendLine().appendLine()

            // Impacts
            if (s.impacts.isNotEmpty()) {
                val impactValues = s.impacts.joinToString("\n") {
                    serializeImportedImpact(it)
                }.prependIndent()
                val impactBlock = "impacts {\n$impactValues\n}".prependIndent()
                builder.append(impactBlock)
                builder.appendLine()
            }

            builder.append("}")
            return builder
        }

        private fun serializeImportedImpact(ii: ImportedImpact, builder: StringBuilder = StringBuilder()): CharSequence {
            val name = sanitizeSymbol(sanitize(ii.name))
            ii.comment?.apply {
                builder.append(asComment(ii.comment))
                builder.appendLine()
            }
            builder.append("${ii.value} ${ii.unitSymbol} $name")
            return builder
        }
    }
}
