package ch.kleis.lcaac.plugin.imports.simapro

import ch.kleis.lcaac.plugin.ide.imports.simapro.SubstanceImportMode
import ch.kleis.lcaac.plugin.imports.ModelWriter
import ch.kleis.lcaac.plugin.imports.shared.serializer.ProcessSerializer
import org.openlca.simapro.csv.process.ProcessBlock
import java.io.File


class SimaproProcessRenderer(mode: SubstanceImportMode) {
    private val mapper = SimaproProcessMapper.of(mode)
    var nbProcesses: Int = 0


    fun render(processBlock: ProcessBlock, writer: ModelWriter) {
        val subFolder = if (processBlock.category() == null) "" else "${processBlock.category()}${File.separatorChar}"
        val process = mapper.map(processBlock)
        val str = ProcessSerializer.serialize(process)

        writer.writeFile("processes${File.separatorChar}$subFolder${process.uid}.lca", str)
        nbProcesses++
    }

}
