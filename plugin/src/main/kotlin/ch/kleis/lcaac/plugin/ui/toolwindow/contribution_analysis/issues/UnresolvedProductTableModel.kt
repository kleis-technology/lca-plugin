package ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.issues

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.value.FromProcessRefValue
import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.ui.toolwindow.FloatingPointRepresentation
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class UnresolvedProductTableModel(
    private val analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
    private val requestedProducts: List<ProductValue<BasicNumber>> = analysis.getEntryPoint()
        .products
        .map { it.product },
    unresolvedProducts: List<ProductValue<BasicNumber>> = analysis.getControllableProducts(),
) : TableModel {
    private val unresolvedProducts = unresolvedProducts.sortedBy { it.getUID() }
    override fun getRowCount(): Int {
        return unresolvedProducts.size
    }

    override fun getColumnCount(): Int {
        return 5 + requestedProducts.size
    }

    override fun getColumnName(columnIndex: Int): String {
        if (columnIndex == 0) return "name"
        if (columnIndex == 1) return "process"
        if (columnIndex == 2) return "params"
        if (columnIndex == 3) return "labels"
        if (columnIndex == 4) return "unit"
        val requestedProduct = requestedProducts[columnIndex - 5]
        return "${requestedProduct.name} [${requestedProduct.referenceUnit.symbol}]"
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        return if (columnIndex <= 4) String::class.java
        else FloatingPointRepresentation::class.java
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val product = unresolvedProducts[rowIndex]
        if (columnIndex == 0) return product.name
        if (columnIndex == 1) return product.fromProcessRef?.name ?: ""
        if (columnIndex == 2) return product.fromProcessRef?.renderArguments() ?: ""
        if (columnIndex == 3) return product.fromProcessRef?.renderLabels() ?: ""
        if (columnIndex == 4) return "${product.referenceUnit.symbol}"
        val offset = columnIndex - 5
        val requestedProduct = requestedProducts[offset]
        val contribution = analysis.getPortContribution(requestedProduct, product)
        return FloatingPointRepresentation.of(contribution.amount.value)
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
    }

    override fun addTableModelListener(l: TableModelListener?) {
    }

    override fun removeTableModelListener(l: TableModelListener?) {
    }

    private fun FromProcessRefValue<BasicNumber>.renderArguments(): String {
        return this.arguments.entries.joinToString { "${it.key}=${it.value}" }
    }

    private fun FromProcessRefValue<BasicNumber>.renderLabels(): String {
        return this.matchLabels.entries.joinToString { "${it.key}=${it.value}" }
    }

}
