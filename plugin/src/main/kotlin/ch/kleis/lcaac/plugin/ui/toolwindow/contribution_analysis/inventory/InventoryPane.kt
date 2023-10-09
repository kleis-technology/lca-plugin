package ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.inventory

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.ui.toolwindow.FloatingPointRepresentation
import ch.kleis.lcaac.plugin.ui.toolwindow.WithHeaderTransferableHandler
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBEmptyBorder
import javax.swing.JLabel
import javax.swing.table.DefaultTableCellRenderer

class InventoryPane(
    analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
    observablePortComparator: Comparator<MatrixColumnIndex<BasicNumber>>,
) {
    val content : JBScrollPane

    init {
        val model = InventoryTableModel(analysis, observablePortComparator)
        val table = JBTable(model)
        table.transferHandler = WithHeaderTransferableHandler()

        val cellRenderer = DefaultTableCellRenderer()
        cellRenderer.horizontalAlignment = JLabel.RIGHT
        table.setDefaultRenderer(FloatingPointRepresentation::class.java, cellRenderer)

        // TODO: Add save as CSV action

        content = JBScrollPane(table)
        content.border = JBEmptyBorder(0)
    }
}
