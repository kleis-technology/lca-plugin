package ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.ui.toolwindow.LcaToolWindowContent
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.impact_assessment.ImpactAssessmentPane
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.inventory.InventoryPane
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.issues.IssuePane
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.supply.SupplyPane
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTabbedPane
import java.awt.BorderLayout
import javax.swing.JPanel

/*
    https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/tool_window
 */

class ContributionAnalysisWindow(
    analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
    observablePortComparator: Comparator<MatrixColumnIndex<BasicNumber>>,
    val project: Project,
    val name: String,
) : LcaToolWindowContent {
    private val content: JPanel

    init {
        /*
            Tab Panes
         */
        val impactAssessmentPane = ImpactAssessmentPane(analysis)
        val inventoryPane = InventoryPane(analysis, observablePortComparator)
        val supplyPane = SupplyPane(analysis, observablePortComparator)
        val issuePane = IssuePane(analysis)

        val tabbed = JBTabbedPane()
        tabbed.add("Impact assessment", impactAssessmentPane.content)
        tabbed.add("Inventory", inventoryPane.content)
        tabbed.add("Supply", supplyPane.content)
        tabbed.add("Issues (${issuePane.nbIssues})", issuePane.content)

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
