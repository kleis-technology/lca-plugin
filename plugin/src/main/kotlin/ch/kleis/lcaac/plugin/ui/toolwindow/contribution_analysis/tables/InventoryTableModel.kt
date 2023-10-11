package ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.ui.toolwindow.shared.FloatingPointRepresentation
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class InventoryTableModel(
    private val analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
    comparator: Comparator<MatrixColumnIndex<BasicNumber>>,
    substances: List<SubstanceValue<BasicNumber>> = analysis.getSubstances(),
    private val requestedProducts: List<ProductValue<BasicNumber>> = analysis.entryPoint
        .products
        .map { it.product },
) : TableModel {
    private val substances= substances.sortedWith(comparator)
    private val displayTotal = requestedProducts.size > 1
    // 5 + 1 if more than products: type, name, compartment, sub_compartment, unit, [total]
    private val columnPrefix = if (displayTotal) 6 else 5

    override fun getRowCount(): Int {
        return substances.size
    }

    override fun getColumnCount(): Int {
        return columnPrefix + requestedProducts.size
    }

    override fun getColumnName(columnIndex: Int): String {
        return when {
            columnIndex == 0 -> "type"
            columnIndex == 1 -> "name"
            columnIndex == 2 -> "compartment"
            columnIndex == 3 -> "sub_compartment"
            columnIndex == 4 -> "unit"
            displayTotal && columnIndex == 5 -> "total"
            else -> {
                val product = requestedProducts[columnIndex - columnPrefix]
                product.name
            }
        }
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        if (columnIndex <= 4) return String::class.java
        return FloatingPointRepresentation::class.java
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    @Suppress("DuplicatedCode")
    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val substance = substances[rowIndex]
        return when {
            columnIndex == 0 -> substance.type()
            columnIndex == 1 -> substance.name()
            columnIndex ==2 -> substance.compartment()
            columnIndex == 3 -> substance.subCompartment()
            columnIndex == 4 -> {
                val total = analysis.supplyOf(substance)
                "${total.unit.symbol}"
            }
            displayTotal && columnIndex == 5 -> {
                val total = analysis.supplyOf(substance)
                FloatingPointRepresentation.of(total.amount.value)
            }
            else -> {
                val product = requestedProducts[columnIndex - columnPrefix]
                val quantity = analysis.allocatedSupplyOf(substance, product)
                FloatingPointRepresentation.of(quantity.amount.value)
            }
        }
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
