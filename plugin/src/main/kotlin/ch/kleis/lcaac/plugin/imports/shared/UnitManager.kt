package ch.kleis.lcaac.plugin.imports.shared

import ch.kleis.lcaac.plugin.imports.model.ImportedUnit
import com.intellij.openapi.diagnostic.Logger

/*
    Stateful.
 */
class UnitManager {
    private val importedUnitsByRef: MutableMap<String, ImportedUnit> = mutableMapOf()
    private var numberOfAddInvocations: Int = 0

    companion object {
        val LOG = Logger.getInstance(UnitManager::class.java)
    }

    fun add(unit: ImportedUnit) {
        numberOfAddInvocations += 1
        if (importedUnitsByRef.containsKey(unit.ref())) {
            LOG.warn("Reference ${unit.ref()} already exists: $unit skipped")
            return
        }
        importedUnitsByRef[unit.ref()] = unit
    }

    fun getNumberOfAddInvocations(): Int = numberOfAddInvocations
}
