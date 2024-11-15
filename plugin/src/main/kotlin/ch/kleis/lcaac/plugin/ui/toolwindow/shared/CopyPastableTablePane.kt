package ch.kleis.lcaac.plugin.ui.toolwindow.shared

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBEmptyBorder
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*
import javax.swing.table.TableModel


class CopyPastableTablePane(
    private val model: TableModel,
    private val project: Project,
    private val defaultFilename: String = "data.csv",
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
        val table = JBTable(model)
        table.transferHandler = WithHeaderTransferableHandler()
        table.autoCreateRowSorter = true
        val cellRenderer = QuantityRenderer
        cellRenderer.horizontalAlignment = JLabel.RIGHT
        table.setDefaultRenderer(Double::class.java, cellRenderer)
        table.autoResizeMode = if (model.columnCount > 24) JTable.AUTO_RESIZE_OFF
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
            Save action
         */
        button.addActionListener {
            val descriptor = FileSaverDescriptor("Save as CSV", "Save data as CSV file")
            val saveFileDialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project)
            val vf = saveFileDialog.save(project.projectFile, defaultFilename) ?: return@addActionListener
            val task = SaveTableModelTask(project, model, vf.file)
            ProgressManager.getInstance().run(task)
        }
    }
}
