package ch.kleis.lcaac.plugin.imports.ecospold

import ch.kleis.lcaac.core.prelude.Prelude
import ch.kleis.lcaac.plugin.imports.ModelWriter
import ch.kleis.lcaac.plugin.imports.ecospold.EcoSpoldImporter.ProcessDictRecord
import ch.kleis.lcaac.plugin.imports.ecospold.model.ActivityDataset
import ch.kleis.lcaac.plugin.imports.shared.UnitManager
import ch.kleis.lcaac.plugin.imports.shared.serializer.ProcessSerializer
import ch.kleis.lcaac.plugin.imports.util.StringUtils
import java.io.File

class EcoSpoldProcessRenderer(
    unitManager: UnitManager,
    processDict: Map<String, ProcessDictRecord>,
    private val writer: ModelWriter,
    methodName: String,
) {
    private val processMapper = EcoSpoldProcessMapper(processDict, unitManager, methodName)

    var nbProcesses: Int = 0
        private set

    fun render(
        activityDataset: ActivityDataset,
        processComment: String,
    ) {
        nbProcesses++

        val category = category(activityDataset)
        val subFolder = if (category == null) "" else "${category}${File.separatorChar}"
        val process = processMapper.map(activityDataset)
        process.comments.add(processComment)
        val strProcess = ProcessSerializer.serialize(process)

        writer.writeRotateFile("processes${File.separatorChar}$subFolder${process.uid}", strProcess)
    }

    private fun category(data: ActivityDataset): String? {
        val desc = data.description.classifications
            .firstOrNull { it.system == "EcoSpold01Categories" }
            ?.value
        return desc?.let { Prelude.sanitize(it) }
    }
}
