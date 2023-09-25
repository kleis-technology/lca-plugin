package ch.kleis.lcaac.plugin.imports.simapro.substance

import ch.kleis.lcaac.plugin.imports.ModelWriter
import ch.kleis.lcaac.plugin.imports.shared.serializer.SubstanceSerializer
import ch.kleis.lcaac.plugin.imports.util.StringUtils.sanitize
import org.openlca.simapro.csv.enums.ElementaryFlowType
import org.openlca.simapro.csv.refdata.ElementaryFlowBlock
import org.openlca.simapro.csv.refdata.ElementaryFlowRow
import java.io.File


class SimaproSubstanceRenderer {
    var nbSubstances = 0

    fun render(block: ElementaryFlowBlock, writer: ModelWriter) {
        val compartimentRaw = block.type().compartment().lowercase()
        val compartiment = sanitize(compartimentRaw)
        val type = block.type()
        block.flows().forEach { render(it, type, compartiment, writer) }
    }

    private fun render(
        element: ElementaryFlowRow,
        type: ElementaryFlowType,
        compartment: String,
        writer: ModelWriter
    ) {
        val uid = sanitize(element.name())
        val substance = SimaproSubstanceMapper.map(element, type, compartment)
        val str = SubstanceSerializer.serialize(substance)
        writer.write(
            "substances${File.separatorChar}$compartment${File.separatorChar}${uid}.lca",
            str
        )
        nbSubstances++
    }


}
