package ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.inventory

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.value.FullyQualifiedSubstanceValue
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.lang.value.PartiallyQualifiedSubstanceValue
import ch.kleis.lcaac.core.lang.value.SubstanceValue
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.ui.toolwindow.FloatingPointRepresentation
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class InventoryTableModel(
    private val analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
    comparator: Comparator<MatrixColumnIndex<BasicNumber>>,
    observableSubstances: List<SubstanceValue<BasicNumber>> = analysis.getObservableSubstances(),
) : TableModel {
    private val observableSubstances: List<SubstanceValue<BasicNumber>> = observableSubstances.sortedWith(comparator)

    override fun getRowCount(): Int {
        return observableSubstances.size
    }

    override fun getColumnCount(): Int {
        return 6
    }

    override fun getColumnName(columnIndex: Int): String {
        if (columnIndex == 0) return "type"
        if (columnIndex == 1) return "name"
        if (columnIndex == 2) return "compartment"
        if (columnIndex == 3) return "sub_compartment"
        if (columnIndex == 4) return "unit"
        return "amount"
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        if (columnIndex <= 4) return String::class.java
        return FloatingPointRepresentation::class.java
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val substance = observableSubstances[rowIndex]
        if (columnIndex == 0) return substance.type()
        if (columnIndex == 1) return substance.name()
        if (columnIndex == 2) return substance.compartment()
        if (columnIndex == 3) return substance.subCompartment()
        val supply = analysis.supplyOf(substance)
        if (columnIndex == 4) return "${supply.unit.symbol}"
        return FloatingPointRepresentation.of(supply.amount.value)
    }

    private fun SubstanceValue<BasicNumber>.name(): String = when(this) {
        is FullyQualifiedSubstanceValue -> this.name
        is PartiallyQualifiedSubstanceValue -> this.name
    }

    private fun SubstanceValue<BasicNumber>.type(): String = when(this) {
        is FullyQualifiedSubstanceValue -> this.type.value
        is PartiallyQualifiedSubstanceValue -> ""
    }

    private fun SubstanceValue<BasicNumber>.compartment(): String = when(this) {
        is FullyQualifiedSubstanceValue -> this.compartment
        is PartiallyQualifiedSubstanceValue -> ""
    }

    private fun SubstanceValue<BasicNumber>.subCompartment(): String = when(this) {
        is FullyQualifiedSubstanceValue -> this.subcompartment ?: ""
        is PartiallyQualifiedSubstanceValue -> ""
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
    }

    override fun addTableModelListener(l: TableModelListener?) {
    }

    override fun removeTableModelListener(l: TableModelListener?) {
    }
}
