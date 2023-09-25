package ch.kleis.lcaac.plugin.imports.ecospold

import ch.kleis.lcaac.plugin.imports.ModelWriter
import ch.kleis.lcaac.plugin.imports.ecospold.EcospoldImporter.ProcessDictRecord
import ch.kleis.lcaac.plugin.imports.ecospold.model.ActivityDataset
import ch.kleis.lcaac.plugin.imports.shared.serializer.ProcessSerializer
import ch.kleis.lcaac.plugin.imports.util.StringUtils
import java.io.File

class EcospoldProcessRenderer {

    var nbProcesses: Int = 0
        private set

    fun render(
        data: ActivityDataset,
        w: ModelWriter,
        processDict: Map<String, ProcessDictRecord>,
        processComment: String,
        methodName: String
    ) {
        nbProcesses++

        val category = category(data)

        val subFolder = if (category == null) "" else "${category}${File.separatorChar}"
        val process = EcoSpoldProcessMapper.map(data, processDict, methodName)
        process.comments.add(processComment)
        val strProcess = ProcessSerializer.serialize(process)

        w.write(
            "processes${File.separatorChar}$subFolder${process.uid}",
            strProcess, index = false, closeAfterWrite = true
        )
    }

    private fun category(data: ActivityDataset): String? {
        val desc = data.description.classifications
            .firstOrNull { it.system == "EcoSpold01Categories" }
            ?.value
        return desc?.let { StringUtils.sanitize(it) }
    }


}
