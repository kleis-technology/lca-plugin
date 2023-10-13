package ch.kleis.lcaac.plugin.imports.simapro.substance

import ch.kleis.lcaac.core.prelude.Prelude.Companion.sanitize
import ch.kleis.lcaac.plugin.imports.ModelWriter
import ch.kleis.lcaac.plugin.imports.shared.serializer.SubstanceSerializer
import org.openlca.simapro.csv.enums.ElementaryFlowType
import org.openlca.simapro.csv.refdata.ElementaryFlowBlock
import org.openlca.simapro.csv.refdata.ElementaryFlowRow
import java.nio.file.Paths


class SimaproSubstanceRenderer {
    var nbSubstances = 0

    fun render(block: ElementaryFlowBlock, writer: ModelWriter) {
        val compartmentRaw = block.type().compartment().lowercase()
        val compartment = sanitize(compartmentRaw)
        val type = block.type()
        block.flows().forEach { render(it, type, compartment, writer) }
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
        writer.writeFile(Paths.get("substances", compartment, "$uid.lca").toString(), str)
        nbSubstances++
    }
}
