package ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.value.FromProcessRefValue
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.MyBundle
import ch.kleis.lcaac.plugin.ui.toolwindow.shared.FloatingPointRepresentation
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class SupplyTableModel(
    private val analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
    comparator: Comparator<MatrixColumnIndex<BasicNumber>>,
    products: List<ProductValue<BasicNumber>> = analysis.getProducts(),
    private val requestedProducts: List<ProductValue<BasicNumber>> = analysis.entryPoint
        .products
        .map { it.product },
) : TableModel {
    private val products: List<ProductValue<BasicNumber>> = products.sortedWith(comparator)
    private val displayTotal = requestedProducts.size > 1
    // 5 columns + 1 if more than one product: name, process, params, labels, unit, [total]
    private val columnPrefix = if (displayTotal) 6 else 5

    override fun getRowCount(): Int {
        return products.size
    }

    override fun getColumnCount(): Int {
        return columnPrefix + requestedProducts.size
    }

    override fun getColumnName(columnIndex: Int): String {
        return when {
            columnIndex == 0 -> MyBundle.message("lca.results.tables.columns.name")
            columnIndex == 1 -> MyBundle.message("lca.results.tables.columns.process")
            columnIndex == 2 -> MyBundle.message("lca.results.tables.columns.params")
            columnIndex == 3 -> MyBundle.message("lca.results.tables.columns.labels")
            columnIndex == 4 -> MyBundle.message("lca.results.tables.columns.unit")
            displayTotal && columnIndex == 5 -> MyBundle.message("lca.results.tables.columns.total")
            else -> requestedProducts[columnIndex - columnPrefix].name
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
        val product = products[rowIndex]
        return when {
            columnIndex == 0 -> product.name
            columnIndex == 1 -> product.fromProcessRef?.name ?: ""
            columnIndex == 2 -> product.fromProcessRef?.renderArguments() ?: ""
            columnIndex == 3 -> product.fromProcessRef?.renderLabels() ?: ""
            columnIndex == 4 -> {
                val total = analysis.supplyOf(product)
                "${total.unit.symbol}"
            }
            displayTotal && columnIndex == 5 -> {
                val total = analysis.supplyOf(product)
                FloatingPointRepresentation.of(total.amount.value)
            }
            else -> {
                val requestedProduct = requestedProducts[columnIndex - columnPrefix]
                val quantity = analysis.allocatedSupplyOf(product, requestedProduct)
                FloatingPointRepresentation.of(quantity.amount.value)
            }
        }
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
