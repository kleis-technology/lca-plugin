package ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.impact_assessment

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.value.IndicatorValue
import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.ui.toolwindow.FloatingPointRepresentation
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class ImpactAssessmentTableModel(
    private val analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
    private val requestedProducts: List<ProductValue<BasicNumber>> = analysis.getEntryPoint()
        .products
        .map { it.product },
    indicators: List<IndicatorValue<BasicNumber>> = analysis.getIndicators(),
) : TableModel {
    private val indicators: List<IndicatorValue<BasicNumber>> = indicators.sortedBy { it.getDisplayName() }

    override fun getRowCount(): Int {
        return indicators.size
    }

    override fun getColumnCount(): Int {
        return 2 + requestedProducts.size
    }

    override fun getColumnName(columnIndex: Int): String {
        if (columnIndex == 0) {
            return "indicator"
        }

        if (columnIndex == 1) {
            return "unit"
        }

        val product = requestedProducts[columnIndex - 2]
        return "${product.getShortName()} [${product.referenceUnit()}]"
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
        if (columnIndex == 0) {
            return indicator.getDisplayName()
        }
        if (columnIndex == 1) {
            return "${indicator.referenceUnit.symbol}"
        }

        val product = requestedProducts[columnIndex - 2]
        val contribution = analysis.getUnitaryImpacts(product)[indicator]?.amount?.value ?: 0.0
        return FloatingPointRepresentation.of(contribution)
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
