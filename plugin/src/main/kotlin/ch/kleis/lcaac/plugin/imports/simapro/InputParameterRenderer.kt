package ch.kleis.lcaac.plugin.imports.simapro

import ch.kleis.lcaac.plugin.imports.ModelWriter
import ch.kleis.lcaac.plugin.imports.util.StringUtils.asComment
import ch.kleis.lcaac.plugin.imports.util.StringUtils.trimTrailingNonPrinting
import org.openlca.simapro.csv.refdata.InputParameterBlock
import org.openlca.simapro.csv.refdata.InputParameterRow

class InputParameterRenderer {
    var nbParameters = 0
    fun render(block: InputParameterBlock, writer: ModelWriter) {
        if (block.parameters().size > 0) {
            val vars = block.parameters().joinToString("\n") { render(it) }
            writer.writeAppendFile(
                "main", """
                    |
                    |variables {
                    |${vars.prependIndent()}
                    |}
                    |
                    """.trimMargin())
            nbParameters += block.parameters().size
        }
    }

    private fun render(param: InputParameterRow): String =
        """
        |${asComment(trimTrailingNonPrinting(param.comment()))}
        |${param.name()} = ${param.value()} u
        """.trimMargin()
}
