package ch.kleis.lcaac.plugin.imports.ecospold

import ch.kleis.lcaac.plugin.imports.ModelWriter
import ch.kleis.lcaac.plugin.imports.ecospold.EcoSpoldImporter.ProcessDictRecord
import ch.kleis.lcaac.plugin.imports.ecospold.model.ActivityDataset
import ch.kleis.lcaac.plugin.imports.shared.serializer.ProcessSerializer
import ch.kleis.lcaac.plugin.imports.util.StringUtils
import java.io.File

class EcoSpoldProcessRenderer {

    var nbProcesses: Int = 0
        private set

    fun render(
        data: ActivityDataset,
        processDict: Map<String, ProcessDictRecord>,
        knownUnits: Set<String>,
        writer: ModelWriter,
        processComment: String,
        methodName: String
    ) {
        nbProcesses++

        val category = category(data)
        val subFolder = if (category == null) "" else "${category}${File.separatorChar}"
        val process = EcoSpoldProcessMapper.map(
            process = data,
            processDict = processDict,
            knownUnits = knownUnits,
            methodName = methodName,
            )
        process.comments.add(processComment)
        val strProcess = ProcessSerializer.serialize(process)

        writer.writeRotateFile("processes${File.separatorChar}$subFolder${process.uid}", strProcess)
    }

    private fun category(data: ActivityDataset): String? {
        val desc = data.description.classifications
            .firstOrNull { it.system == "EcoSpold01Categories" }
            ?.value
        return desc?.let { StringUtils.sanitize(it) }
    }
}
