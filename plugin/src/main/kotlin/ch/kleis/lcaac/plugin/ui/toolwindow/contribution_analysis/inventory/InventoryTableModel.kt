package ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.inventory

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.ui.toolwindow.FloatingPointRepresentation
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class InventoryTableModel(
    private val analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
    comparator: Comparator<MatrixColumnIndex<BasicNumber>>,
    substances: List<SubstanceValue<BasicNumber>> = analysis.getSubstances(),
    private val requestedProducts: List<ProductValue<BasicNumber>> = analysis.getEntryPoint()
        .products
        .map { it.product },
) : TableModel {
    private val substances= substances.sortedWith(comparator)
    private val displayTotal = requestedProducts.size > 1
    private val columnPrefix = if (displayTotal) 6 else 5

    override fun getRowCount(): Int {
        return substances.size
    }

    override fun getColumnCount(): Int {
        return columnPrefix + requestedProducts.size
    }

    override fun getColumnName(columnIndex: Int): String {
        if (columnIndex == 0) return "type"
        if (columnIndex == 1) return "name"
        if (columnIndex == 2) return "compartment"
        if (columnIndex == 3) return "sub_compartment"
        if (columnIndex == 4) return "unit"
        if (displayTotal && columnIndex == 5) return "total"
        val product = requestedProducts[columnIndex - columnPrefix]
        return product.name
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        if (columnIndex <= 4) return String::class.java
        return FloatingPointRepresentation::class.java
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val substance = substances[rowIndex]
        if (columnIndex == 0) return substance.type()
        if (columnIndex == 1) return substance.name()
        if (columnIndex == 2) return substance.compartment()
        if (columnIndex == 3) return substance.subCompartment()
        val total = analysis.supplyOf(substance)
        if (columnIndex == 4) return "${total.unit.symbol}"
        if (displayTotal && columnIndex == 5) {
            return FloatingPointRepresentation.of(total.amount.value)
        }
        val product = requestedProducts[columnIndex - columnPrefix]
        val quantity = analysis.allocatedSupplyOf(substance, product)
        return FloatingPointRepresentation.of(quantity.amount.value)
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
