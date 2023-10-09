package ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.issues

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.ui.toolwindow.FloatingPointRepresentation
import ch.kleis.lcaac.plugin.ui.toolwindow.WithHeaderTransferableHandler
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBEmptyBorder
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.table.DefaultTableCellRenderer

class IssuePane(
    analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
) {
    val content : JPanel
    val nbIssues : Int

    init {
        val cellRenderer = DefaultTableCellRenderer()

        val unresolvedProductTableModel = UnresolvedProductTableModel(analysis)
        val unresolvedProductTable = JBTable(unresolvedProductTableModel)
        unresolvedProductTable.transferHandler = WithHeaderTransferableHandler()
        cellRenderer.horizontalAlignment = JLabel.RIGHT
        unresolvedProductTable.setDefaultRenderer(FloatingPointRepresentation::class.java, cellRenderer)
        val unresolvedProductPane = JBScrollPane(unresolvedProductTable)

        val nonCharacterizedSubstanceTableModel = NonCharacterizedSubstanceTableModel(analysis)
        val nonCharacterizedSubstanceTable = JBTable(nonCharacterizedSubstanceTableModel)
        nonCharacterizedSubstanceTable.transferHandler = WithHeaderTransferableHandler()
        cellRenderer.horizontalAlignment = JLabel.RIGHT
        nonCharacterizedSubstanceTable.setDefaultRenderer(FloatingPointRepresentation::class.java, cellRenderer)
        val nonCharacterizedSubstancePane = JBScrollPane(nonCharacterizedSubstanceTable)

        val tabbed = JBTabbedPane()
        tabbed.add("Products (${unresolvedProductTableModel.rowCount})", unresolvedProductPane)
        tabbed.add("Substances (${nonCharacterizedSubstanceTableModel.rowCount})", nonCharacterizedSubstancePane)

        content = JPanel(BorderLayout())
        content.add(tabbed, BorderLayout.CENTER)
        content.border = JBEmptyBorder(0)

        nbIssues = unresolvedProductTableModel.rowCount + nonCharacterizedSubstanceTableModel.rowCount
    }
}
