package ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.value.IndicatorValue
import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.ui.toolwindow.shared.FloatingPointRepresentation
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class ImpactAssessmentTableModel(
    private val analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
    private val requestedProducts: List<ProductValue<BasicNumber>> = analysis.entryPoint
        .products
        .map { it.product },
    indicators: List<IndicatorValue<BasicNumber>> = analysis.getIndicators(),
) : TableModel {
    private val indicators: List<IndicatorValue<BasicNumber>> = indicators.sortedBy { it.getDisplayName() }
    private val displayTotal = requestedProducts.size > 1
    // 2 + 1 if more than one product: indicator, unit, [total]
    private val columnPrefix = if (displayTotal) 3 else 2

    override fun getRowCount(): Int {
        return indicators.size
    }

    override fun getColumnCount(): Int {
        return columnPrefix + requestedProducts.size
    }

    override fun getColumnName(columnIndex: Int): String {
        return when {
            columnIndex == 0 -> "indicator"
            columnIndex == 1 -> "unit"
            displayTotal && columnIndex == 2 -> "total"
            else -> {
                val product = requestedProducts[columnIndex - columnPrefix]
                product.getShortName()
            }
        }
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        return when (columnIndex) {
            0 -> String::class.java
            1 -> String::class.java
            else -> FloatingPointRepresentation::class.java
        }
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val indicator = indicators[rowIndex]
        return when {
            columnIndex == 0 -> indicator.getDisplayName()
            columnIndex == 1 -> "${indicator.referenceUnit.symbol}"
            displayTotal && columnIndex == 2 -> {
                val total = analysis.supplyOf(indicator)
                FloatingPointRepresentation.of(total.amount.value)
            }
            else -> {
                val product = requestedProducts[columnIndex - columnPrefix]
                val contribution = analysis.getPortContribution(product, indicator)
                FloatingPointRepresentation.of(contribution.amount.value)
            }
        }
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        // Read Only
    }

    override fun addTableModelListener(l: TableModelListener?) {
        // Read Only
    }

    override fun removeTableModelListener(l: TableModelListener?) {
        // Read Only
    }
}
