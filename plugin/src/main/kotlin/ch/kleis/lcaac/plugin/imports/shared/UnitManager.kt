package ch.kleis.lcaac.plugin.imports.shared

import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.prelude.Prelude
import ch.kleis.lcaac.plugin.imports.model.ImportedUnit
import com.intellij.openapi.diagnostic.Logger

/*
    Stateful.
 */
class UnitManager {
    private val importedUnitsByRef: MutableMap<String, ImportedUnit> = mutableMapOf()
    private val knownUnitRefs: MutableSet<String> = Prelude.unitMap<BasicNumber>().keys.toMutableSet()
    private var numberOfAddInvocations: Int = 0

    companion object {
        val LOG = Logger.getInstance(UnitManager::class.java)
    }

    fun add(unit: ImportedUnit, next: (ImportedUnit) -> Unit) {
        numberOfAddInvocations += 1
        if (isAlreadyKnown(unit)) return
        val ref = unit.ref()
        importedUnitsByRef[ref] = unit
        knownUnitRefs.add(ref)
        next(unit)
    }

    private fun isAlreadyKnown(unit: ImportedUnit): Boolean {
        return knownUnitRefs.contains(unit.ref())
    }

    fun getNumberOfAddInvocations(): Int = numberOfAddInvocations
}
