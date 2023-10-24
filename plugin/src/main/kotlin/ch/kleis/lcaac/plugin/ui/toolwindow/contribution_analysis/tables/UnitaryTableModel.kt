package ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.value.IndicatorValue
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.ui.toolwindow.shared.FloatingPointRepresentation
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class UnitaryTableModel(private val analysis: ContributionAnalysis<BasicNumber, BasicMatrix>) : TableModel {
    private val indicators: List<IndicatorValue<BasicNumber>> = analysis.getIndicators()
    private val products = analysis.getObservablePorts()
    private val unitaryImpacts = (0 until products.size()).map {
        analysis.getUnitaryImpacts(products[it])
    }
    private val signs = (0 until products.size()).map {
        if (analysis.supplyOf(products[it]).amount.value < 0.0) {
            -1
        } else {
            1
        }
    }

    override fun getRowCount(): Int = products.size()

    override fun getColumnCount(): Int = 1 + indicators.size

    override fun getColumnName(p0: Int): String = when (p0) {
        0 -> "Product"
        else -> indicators[p0 - 1].getDisplayName()
    }

    override fun getColumnClass(p0: Int): Class<*> = when (p0) {
        0 -> String::class.java
        else -> FloatingPointRepresentation::class.java
    }

    override fun isCellEditable(p0: Int, p1: Int): Boolean = false

    override fun getValueAt(rowIdx: Int, colIdx: Int): Any = when {
        colIdx == 0 -> products[rowIdx].getDisplayName()
        else -> {
            val indicator = indicators[colIdx - 1]
            val value = unitaryImpacts[rowIdx][indicator]
            FloatingPointRepresentation.of(signs[rowIdx] * (value?.amount?.value ?: 0.0))
        }
    }

    override fun setValueAt(p0: Any?, p1: Int, p2: Int) {
    }

    override fun addTableModelListener(p0: TableModelListener?) {
    }

    override fun removeTableModelListener(p0: TableModelListener?) {}
}