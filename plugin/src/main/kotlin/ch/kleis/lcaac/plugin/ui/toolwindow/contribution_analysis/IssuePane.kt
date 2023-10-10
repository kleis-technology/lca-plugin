package ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables.InventoryTableModel
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables.SupplyTableModel
import ch.kleis.lcaac.plugin.ui.toolwindow.shared.CopyPastableTablePane
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.JBEmptyBorder
import java.awt.BorderLayout
import javax.swing.JPanel

class IssuePane(
    analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
    comparator: Comparator<MatrixColumnIndex<BasicNumber>>,
    project: Project,
) {
    val content : JPanel
    val nbIssues : Int

    init {
        val unresolvedProductTableModel = SupplyTableModel(analysis, comparator, analysis.getUnresolvedProducts())
        val unresolvedProductPane = CopyPastableTablePane(unresolvedProductTableModel, project, "products_without_processes.csv").content

        val nonCharacterizedSubstanceTableModel = InventoryTableModel(analysis, comparator, analysis.getNonCharacterizedSubstances())
        val nonCharacterizedSubstancePane = CopyPastableTablePane(nonCharacterizedSubstanceTableModel, project, "substances_without_impacts.csv").content

        val tabbed = JBTabbedPane()
        tabbed.add("Products without processes (${unresolvedProductTableModel.rowCount})", unresolvedProductPane)
        tabbed.add("Substances without impacts (${nonCharacterizedSubstanceTableModel.rowCount})", nonCharacterizedSubstancePane)

        content = JPanel(BorderLayout())
        content.add(tabbed, BorderLayout.CENTER)
        content.border = JBEmptyBorder(0)

        nbIssues = unresolvedProductTableModel.rowCount + nonCharacterizedSubstanceTableModel.rowCount
    }
}
