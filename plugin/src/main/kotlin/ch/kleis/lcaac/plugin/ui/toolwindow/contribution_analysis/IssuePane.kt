package ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.ui.toolwindow.shared.FloatingPointRepresentation
import ch.kleis.lcaac.plugin.ui.toolwindow.shared.WithHeaderTransferableHandler
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables.InventoryTableModel
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables.SupplyTableModel
import ch.kleis.lcaac.plugin.ui.toolwindow.shared.CopyPastableTablePane
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
    comparator: Comparator<MatrixColumnIndex<BasicNumber>>,
) {
    val content : JPanel
    val nbIssues : Int

    init {
        val unresolvedProductTableModel = SupplyTableModel(analysis, comparator, analysis.getUnresolvedProducts())
        val unresolvedProductPane = CopyPastableTablePane(unresolvedProductTableModel).content

        val nonCharacterizedSubstanceTableModel = InventoryTableModel(analysis, comparator, analysis.getNonCharacterizedSubstances())
        val nonCharacterizedSubstancePane = CopyPastableTablePane(nonCharacterizedSubstanceTableModel).content

        val tabbed = JBTabbedPane()
        tabbed.add("Products without processes (${unresolvedProductTableModel.rowCount})", unresolvedProductPane)
        tabbed.add("Substances without impacts (${nonCharacterizedSubstanceTableModel.rowCount})", nonCharacterizedSubstancePane)

        content = JPanel(BorderLayout())
        content.add(tabbed, BorderLayout.CENTER)
        content.border = JBEmptyBorder(0)

        nbIssues = unresolvedProductTableModel.rowCount + nonCharacterizedSubstanceTableModel.rowCount
    }
}
