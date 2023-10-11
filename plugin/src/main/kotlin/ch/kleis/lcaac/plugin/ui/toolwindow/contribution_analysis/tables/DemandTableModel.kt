package ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.ui.toolwindow.shared.FloatingPointRepresentation
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class DemandTableModel(
    private val analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
) : TableModel {
    private val productExchanges = analysis.entryPoint.products

    override fun getRowCount(): Int {
        return productExchanges.size
    }

    override fun getColumnCount(): Int {
        return 4
    }

    override fun getColumnName(columnIndex: Int): String {
        return when(columnIndex) {
            0 -> "name"
            1 -> "unit"
            2 -> "amount"
            3 -> "allocation [percent]"
            else -> throw EvaluatorException("invalid column index")
        }
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        return when(columnIndex) {
            0, 1 -> String::class.java
            2, 3 -> FloatingPointRepresentation::class.java
            else -> throw EvaluatorException("invalid column index")
        }
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val exchange = productExchanges[rowIndex]
        return when(columnIndex) {
            0 -> exchange.product.name
            1 -> "${exchange.quantity.unit.symbol}"
            2 -> FloatingPointRepresentation.of(exchange.quantity.amount.value)
            3 -> FloatingPointRepresentation.of(exchange.allocation?.amount?.value ?: 100.0)
            else -> throw EvaluatorException("invalid column index")
        }
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
    }

    override fun addTableModelListener(l: TableModelListener?) {
    }

    override fun removeTableModelListener(l: TableModelListener?) {
    }
}
