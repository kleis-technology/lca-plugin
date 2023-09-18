package ch.kleis.lcaac.plugin.imports.shared.serializer

import ch.kleis.lcaac.plugin.imports.ModelWriter
import ch.kleis.lcaac.plugin.imports.model.ImportedSubstance
import ch.kleis.lcaac.plugin.imports.simapro.sanitizeSymbol

class SubstanceSerializer {

    companion object {

        fun serialize(s: ImportedSubstance): CharSequence {

            val builder = StringBuilder()

            builder.append(
                """

substance ${s.uid} {

    name = "${s.name}"
    type = ${s.type}
    compartment = "${s.compartment}""""
            )
            if (!s.subCompartment.isNullOrBlank()) {
                builder.append(
                    """
    sub_compartment = "${s.subCompartment}""""
                )
            }
            builder.append(
                """
    reference_unit = ${s.referenceUnitSymbol()}

    meta {
"""
            )
            builder.append(ModelWriter.blockKeyValue(s.meta.entries, 8))
            builder.append(
                """
    }

    impacts {"""
            )
            s.impacts.forEach {
                if (it.comment != null) {
                    builder.append(
                        """
        // ${it.comment}"""
                    )
                }
                val name = sanitizeSymbol(ModelWriter.sanitizeAndCompact(it.name))
                builder.append(
                    """
        ${it.value} ${it.unitSymbol} $name"""
                )
            }
            builder.append(
                """
    }
}"""
            )
            return builder
        }
    }
}