package ch.kleis.lcaac.plugin.imports.shared

import ch.kleis.lcaac.plugin.imports.ModelWriter
import ch.kleis.lcaac.plugin.imports.model.ImportedUnit
import ch.kleis.lcaac.plugin.imports.shared.serializer.UnitSerializer

class UnitRenderer(
    private val manager: UnitManager,
    private val serializer: UnitSerializer = UnitSerializer()
) {
    fun render(unit: ImportedUnit, writer: ModelWriter) {
        if (manager.isAlreadyKnown(unit)) return
        manager.add(unit)
        val serialized = serializer.serialize(unit)
        writer.writeAppendFile("unit", serialized)
    }
}
