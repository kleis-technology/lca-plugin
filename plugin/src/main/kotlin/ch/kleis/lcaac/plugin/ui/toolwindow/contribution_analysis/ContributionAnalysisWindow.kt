package ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.ui.toolwindow.LcaToolWindowContent
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables.DemandTableModel
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables.ImpactAssessmentTableModel
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables.InventoryTableModel
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables.SupplyTableModel
import ch.kleis.lcaac.plugin.ui.toolwindow.shared.CopyPastableTablePane
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTabbedPane
import java.awt.BorderLayout
import javax.swing.JPanel

/*
    https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/tool_window
 */

class ContributionAnalysisWindow(
    analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
    trace: EvaluationTrace<BasicNumber>,
    val project: Project,
    val name: String,
) : LcaToolWindowContent {
    private val content: JPanel

    init {
        val comparator = trace.getComparator()

        /*
            Tab Panes
         */
        val demandPane = CopyPastableTablePane(DemandTableModel(analysis), project, "demand.csv")
        val impactAssessmentPane =
            CopyPastableTablePane(ImpactAssessmentTableModel(analysis), project, "impact_assessment.csv")
        val inventoryPane = CopyPastableTablePane(InventoryTableModel(analysis, comparator), project, "inventory.csv")
        val supplyPane = CopyPastableTablePane(SupplyTableModel(analysis, comparator), project, "supply.csv")
        val issuePane = IssuePane(analysis, comparator, project)
        val tracePane = TracePane(analysis, trace, project)


        val tabbed = JBTabbedPane()
        tabbed.add("Demand", demandPane.content)
        tabbed.add("Impact assessment", impactAssessmentPane.content)
        tabbed.add("Inventory", inventoryPane.content)
        tabbed.add("Supply", supplyPane.content)
        tabbed.add("Issues (${issuePane.nbIssues})", issuePane.content)
        tabbed.add("Trace", tracePane.content)
        tabbed.selectedIndex = 1

        /*
            Content
         */
        content = JPanel(BorderLayout())
        content.add(tabbed, BorderLayout.CENTER)
        content.updateUI()
    }

    override fun getContent(): JPanel {
        return content
    }

}
