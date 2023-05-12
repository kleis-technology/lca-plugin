package ch.kleis.lcaplugin.imports.shared

import ch.kleis.lcaplugin.imports.ModelWriter
import ch.kleis.lcaplugin.imports.model.*

class ProcessSerializer {
    companion object {

        fun serialize(p: ProcessImported): CharSequence {

            val builder = StringBuilder()

            // Meta
            val metaBloc = ModelWriter.blockKeyValue(p.meta.entries, 8)
            builder.append(
                """

process ${p.uid} {

    meta {
$metaBloc
    }
"""
            )
            val blocks = listOf(
                "products" to p.productBlocks,
                "inputs" to p.inputBlocks,
                "emissions" to p.emissionBlocks,
                "resources" to p.resourceBlocks,
                "land_use" to p.landUseBlocks,
            )

            blocks.forEach { blockGrp ->
                blockGrp.second.forEach { block ->
                    val doc = if (block.comment.isNotBlank()) " // ${block.comment}" else ""
                    builder.append(
                        """
    ${blockGrp.first} {$doc
${serialize(block.exchanges, 8)}
    }
"""
                    )
                }
            }


            builder.append("}")
            return builder
        }
    }
}

fun serialize(block: List<ExchangeImported>, pad: Int): CharSequence {
    val prefix = " ".repeat(pad)
    return block.flatMap { serialize(it) }
        .joinTo(StringBuilder(), "\n$prefix", prefix)
}

fun serialize(e: ExchangeImported): List<CharSequence> {
    val comments = e.comments
        .filter { it.isNotBlank() }
        .map { "// $it" }
    val txt = when (e) {
        is ProductImported -> "${e.qty} ${e.unit} ${e.uid} allocate ${e.allocation} percent"
        is InputImported -> "${e.qty} ${e.unit} ${e.uid}"
        is BioExchangeImported -> {
            val sub = e.subCompartment?.let { ", sub_compartment = \"$it\"" } ?: ""
            """${e.qty} ${e.unit} ${e.uid}(compartment = "${e.compartment}"$sub)"""
        }
    }
    return comments + txt
}
