package ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables.TraceTableModel
import ch.kleis.lcaac.plugin.ui.toolwindow.shared.QuantityRenderer
import ch.kleis.lcaac.plugin.ui.toolwindow.shared.SaveTableModelTask
import ch.kleis.lcaac.plugin.ui.toolwindow.shared.WithHeaderTransferableHandler
import com.intellij.icons.AllIcons
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBEmptyBorder
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.EventQueue
import javax.swing.*

class TracePane(
    analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
    trace: EvaluationTrace<BasicNumber>,
    private val project: Project,
) {
    val content: JPanel

    init {
        /*
            Button
         */
        val button = JButton(AllIcons.Actions.MenuSaveall)
        button.preferredSize = Dimension(40, 40)

        /*
            Toolbar
         */
        val toolbar = JToolBar()
        toolbar.border = JBEmptyBorder(0)
        toolbar.layout = BoxLayout(toolbar, BoxLayout.Y_AXIS)
        toolbar.isFloatable = false
        toolbar.add(button)

        /*
            Table
         */
        val table = JBTable(TraceTableModel(analysis, trace))
        table.transferHandler = WithHeaderTransferableHandler()
        table.autoCreateRowSorter = true
        val cellRenderer = QuantityRenderer
        cellRenderer.horizontalAlignment = JLabel.RIGHT
        table.setDefaultRenderer(Double::class.java, cellRenderer)
        table.autoResizeMode = if (table.columnCount > 24) JTable.AUTO_RESIZE_OFF
        else JTable.AUTO_RESIZE_ALL_COLUMNS

        /*
            Menu bar
         */
        val comboBox = ComboBox<MatrixColumnIndex<BasicNumber>>()
        analysis.getControllablePorts().getElements().forEach(comboBox::addItem)
        comboBox.addActionListener {
            if (it.actionCommand == "comboBoxChanged") {
                EventQueue.invokeLater {
                    @Suppress("UNCHECKED_CAST")
                    val indicator = comboBox.selectedItem as MatrixColumnIndex<BasicNumber>
                    table.model = TraceTableModel(analysis, trace, listOf(indicator))
                    table.autoResizeMode = if (table.columnCount > 24) JTable.AUTO_RESIZE_OFF
                    else JTable.AUTO_RESIZE_ALL_COLUMNS
                }
            }
        }

        val menuBar = JMenuBar()
        menuBar.add(JBLabel("Indicator"))
        menuBar.add(JBBox.createHorizontalGlue())
        menuBar.add(comboBox)
        menuBar.add(JBBox.createHorizontalGlue(), BorderLayout.LINE_END)

        /*
            Content
         */
        val scrollPane =
            JBScrollPane(table, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
        content = JPanel(BorderLayout())
        content.add(menuBar, BorderLayout.NORTH)
        content.add(scrollPane, BorderLayout.CENTER)
        content.add(toolbar, BorderLayout.LINE_END)
        content.border = JBEmptyBorder(0)

        /*
            Save action
         */
        button.addActionListener {
            val descriptor = FileSaverDescriptor("Save as CSV", "Save data as CSV file")
            val saveFileDialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project)
            val vf = saveFileDialog.save(project.projectFile, "trace.csv") ?: return@addActionListener
            val task = SaveTableModelTask(project, table.model, vf.file)
            ProgressManager.getInstance().run(task)
        }
    }
}
