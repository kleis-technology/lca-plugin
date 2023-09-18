package ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.ui.toolwindow.FloatingPointRepresentation
import ch.kleis.lcaac.plugin.ui.toolwindow.LcaToolWindowContent
import ch.kleis.lcaac.plugin.ui.toolwindow.WithHeaderTransferableHandler
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.components.JBBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBEmptyBorder
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JMenuBar
import javax.swing.JPanel
import javax.swing.table.DefaultTableCellRenderer

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
            Table pane
         */
        val tableModel = ContributionTableModel(analysis, observablePortComparator)

        val table = JBTable(tableModel)
        table.transferHandler = WithHeaderTransferableHandler()

        val cellRenderer = DefaultTableCellRenderer()
        cellRenderer.horizontalAlignment = JLabel.RIGHT
        table.setDefaultRenderer(FloatingPointRepresentation::class.java, cellRenderer)

        val tablePane = JBScrollPane(table)
        tablePane.border = JBEmptyBorder(0)

        /*
            Menu bar
         */
        val button = JButton(AllIcons.Actions.MenuSaveall)
        button.addActionListener {
            val toolWindow =
                ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return@addActionListener
            val content = ContentFactory.getInstance()
                .createContent(
                    ContributionAnalysisHugeWindow(
                        analysis,
                        observablePortComparator,
                        "lca.dialog.export.info",
                        project
                    ).getContent(),
                    name,
                    false
                )
            toolWindow.contentManager.addContent(content)
            toolWindow.contentManager.setSelectedContent(content)
            toolWindow.show()
        }

        val menuBar = JMenuBar()
        menuBar.add(JBLabel("Contribution analysis"), BorderLayout.LINE_START)
        menuBar.add(JBBox.createHorizontalGlue())
        menuBar.add(button, BorderLayout.LINE_END)

        /*
            Content
         */
        content = JPanel(BorderLayout())
        content.add(menuBar, BorderLayout.PAGE_START)
        content.add(tablePane, BorderLayout.CENTER)
        content.updateUI()
    }

    override fun getContent(): JPanel {
        return content
    }

}