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
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBEmptyBorder
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.Frame
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
        val selectColumnsButton = JButton(AllIcons.General.Filter)
        selectColumnsButton.preferredSize = Dimension(40, 40)

        val saveButton = JButton(AllIcons.Actions.MenuSaveall)
        saveButton.preferredSize = Dimension(40, 40)

        /*
            Toolbar
         */
        val toolbar = JToolBar()
        toolbar.border = JBEmptyBorder(0)
        toolbar.layout = BoxLayout(toolbar, BoxLayout.Y_AXIS)
        toolbar.isFloatable = false
        toolbar.add(selectColumnsButton)
        toolbar.add(saveButton)

        /*
            Table
         */
        val table = JBTable(TraceTableModel(analysis, trace))
        table.transferHandler = WithHeaderTransferableHandler()
        table.autoCreateRowSorter = true
        val cellRenderer = QuantityRenderer
        cellRenderer.horizontalAlignment = JLabel.RIGHT
        table.setDefaultRenderer(Double::class.java, cellRenderer)
        table.autoResizeMode = if (table.model.columnCount > 24) JTable.AUTO_RESIZE_OFF
        else JTable.AUTO_RESIZE_ALL_COLUMNS

        /*
            Content
         */
        val scrollPane =
            JBScrollPane(table, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
        content = JPanel(BorderLayout())
        content.add(scrollPane, BorderLayout.CENTER)
        content.add(toolbar, BorderLayout.LINE_END)
        content.border = JBEmptyBorder(0)

        /*
            Select columns action
         */
        selectColumnsButton.addActionListener {
            EventQueue.invokeLater {
                val dialog = SelectColumnsDialog(
                    availableColumns = analysis.getControllablePorts().getElements(),
                    selectedColumns = (table.model as TraceTableModel).getIndicators(),
                )
                dialog.isVisible = true

                val selectedColumns = dialog.getSelectedColumns()
                table.model = TraceTableModel(analysis, trace, selectedColumns)
                table.autoResizeMode = if (table.model.columnCount > 24) JTable.AUTO_RESIZE_OFF
                else JTable.AUTO_RESIZE_ALL_COLUMNS
            }
        }

        /*
            Save action
         */
        saveButton.addActionListener {
            val descriptor = FileSaverDescriptor("Save as CSV", "Save data as CSV file")
            val saveFileDialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project)
            val vf = saveFileDialog.save(project.projectFile, "trace.csv") ?: return@addActionListener
            val task = SaveTableModelTask(project, table.model, vf.file)
            ProgressManager.getInstance().run(task)
        }
    }

    private class SelectColumnsDialog(
        availableColumns: List<MatrixColumnIndex<BasicNumber>>,
        private var selectedColumns: List<MatrixColumnIndex<BasicNumber>> = emptyList()
    ) : JDialog(null as Frame?, "Select columns", true) {

        init {
            val itemList = JBList(
                availableColumns.sortedBy { it.getShortName() }
            )
            itemList.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
            val listScrollPane = JScrollPane(itemList)
            val okButton = JButton("OK")
            val cancelButton = JButton("Cancel")

            okButton.addActionListener {
                selectedColumns = itemList.selectedValuesList
                dispose()
            }

            cancelButton.addActionListener {
                dispose()
            }

            val buttonPanel = JPanel()
            buttonPanel.add(okButton)
            buttonPanel.add(cancelButton)

            add(listScrollPane, BorderLayout.CENTER)
            add(buttonPanel, BorderLayout.SOUTH)

            setSize(300, 300)
            setLocationRelativeTo(parent)
        }

        fun getSelectedColumns(): List<MatrixColumnIndex<BasicNumber>> {
            return selectedColumns
        }
    }
}
