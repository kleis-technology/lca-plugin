package ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.value.FromProcessRefValue
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.ui.toolwindow.shared.FloatingPointRepresentation
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class SupplyTableModel(
    private val analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
    comparator: Comparator<MatrixColumnIndex<BasicNumber>>,
    products: List<ProductValue<BasicNumber>> = analysis.getProducts(),
    private val requestedProducts: List<ProductValue<BasicNumber>> = analysis.getEntryPoint()
        .products
        .map { it.product },
) : TableModel {
    private val products: List<ProductValue<BasicNumber>> = products.sortedWith(comparator)
    private val displayTotal = requestedProducts.size > 1
    private val columnPrefix = if (displayTotal) 6 else 5

    override fun getRowCount(): Int {
        return products.size
    }

    override fun getColumnCount(): Int {
        return columnPrefix + requestedProducts.size
    }

    override fun getColumnName(columnIndex: Int): String {
        if (columnIndex == 0) return "name"
        if (columnIndex == 1) return "process"
        if (columnIndex == 2) return "params"
        if (columnIndex == 3) return "labels"
        if (columnIndex == 4) return "unit"
        if (displayTotal && columnIndex == 5) return "total"
        return requestedProducts[columnIndex - columnPrefix].name
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
        val product = products[rowIndex]
        if (columnIndex == 0) return product.name
        if (columnIndex == 1) return product.fromProcessRef?.name ?: ""
        if (columnIndex == 2) return product.fromProcessRef?.renderArguments() ?: ""
        if (columnIndex == 3) return product.fromProcessRef?.renderLabels() ?: ""
        val total = analysis.supplyOf(product)
        if (columnIndex == 4) return "${total.unit.symbol}"
        if (displayTotal && columnIndex == 5) return FloatingPointRepresentation.of(total.amount.value)
        val requestedProduct = requestedProducts[columnIndex - columnPrefix]
        val quantity = analysis.allocatedSupplyOf(product, requestedProduct)
        return FloatingPointRepresentation.of(quantity.amount.value)
    }

    private fun FromProcessRefValue<BasicNumber>.renderArguments(): String {
        return this.arguments.entries.joinToString { "${it.key}=${it.value}" }
    }

    private fun FromProcessRefValue<BasicNumber>.renderLabels(): String {
        return this.matchLabels.entries.joinToString { "${it.key}=${it.value}" }
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
    }

    override fun addTableModelListener(l: TableModelListener?) {
    }

    override fun removeTableModelListener(l: TableModelListener?) {
    }
}
