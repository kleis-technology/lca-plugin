package ch.kleis.lcaac.plugin.imports.shared.serializer

import ch.kleis.lcaac.plugin.imports.model.*
import ch.kleis.lcaac.plugin.imports.util.StringUtils
import ch.kleis.lcaac.plugin.imports.util.StringUtils.asCommentList
import ch.kleis.lcaac.plugin.imports.util.StringUtils.merge

object ProcessSerializer {
    fun serialize(e: ImportedExchange): List<CharSequence> {
        val comments = asCommentList(e.comments
            .flatMap(CharSequence::lines)
            .filter { it.isNotBlank() })
        val printCommented = if (e.printAsComment) "// " else ""
        val txt = when (e) {
            is ImportedProductExchange -> "${printCommented}${e.qty} ${e.unit} ${e.uid} allocate ${e.allocation} percent"
            is ImportedImpactExchange -> "${printCommented}${e.qty} ${e.unit} ${e.uid}"

            is ImportedInputExchange -> {
                val fromProcess = e.fromProcess?.let { " from $it" } ?: ""
                "${printCommented}${e.qty} ${e.unit} ${e.uid}${fromProcess}"
            }

            is ImportedBioExchange -> {
                val sub = e.subCompartment?.let { ", sub_compartment = \"$it\"" } ?: ""
                """${printCommented}${e.qty} ${e.unit} ${e.uid}(compartment = "${e.compartment}"$sub)"""
            }
        }
        return comments + txt
    }

    fun serialize(block: Sequence<ImportedExchange>): CharSequence {
        return block.flatMap { serialize(it) }
            .joinTo(StringBuilder(), "\n")
    }

    fun serialize(p: ImportedProcess): CharSequence {

        val builder = StringBuilder()

        // Header
        builder.append("process ${p.uid} {")
        builder.appendLine().appendLine()

        // Meta
        val metaValues = StringUtils.blockKeyValue(p.meta).toString().prependIndent()
        val metaBlock = "meta {\n$metaValues\n}".prependIndent()
        builder.append(metaBlock)
        builder.appendLine().appendLine()

        // Params
        if (p.params.isNotEmpty()) {
            val paramsValues = merge(p.params.map { "${it.symbol} = ${it.value}" }).prependIndent()
            val paramsBlock = "params {\n$paramsValues\n}".prependIndent()
            builder.append(paramsBlock)
            builder.appendLine().appendLine()
        }

        val blocks = listOf(
            "products" to p.productBlocks,
            "inputs" to p.inputBlocks,
            "emissions" to p.emissionBlocks,
            "resources" to p.resourceBlocks,
            "land_use" to p.landUseBlocks,
            "impacts" to p.impactBlocks,
        )

        blocks
            .filter { (_, l) -> l.isNotEmpty() }
            .forEach { (keyword, blockList) ->
                blockList.forEach { block ->
                    val doc = if (block.comment?.isNotBlank() == true) " // ${block.comment}" else ""
                    val exchanges = serialize(block.exchanges).toString().prependIndent()
                    val exchangeBlock = "$keyword {$doc\n$exchanges\n}".prependIndent()
                    builder.append(exchangeBlock)
                    builder.appendLine().appendLine()
                }
            }

        builder.append("}")
        return builder
    }
}
