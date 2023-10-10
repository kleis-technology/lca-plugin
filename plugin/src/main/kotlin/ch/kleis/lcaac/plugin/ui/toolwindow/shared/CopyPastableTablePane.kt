package ch.kleis.lcaac.plugin.ui.toolwindow.shared

import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBEmptyBorder
import javax.swing.JLabel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableModel

class CopyPastableTablePane(
    val model: TableModel
) {
    val content: JBScrollPane

    init {
        val table = JBTable(model)
        table.transferHandler = WithHeaderTransferableHandler()

        val cellRenderer = DefaultTableCellRenderer()
        cellRenderer.horizontalAlignment = JLabel.RIGHT
        table.setDefaultRenderer(FloatingPointRepresentation::class.java, cellRenderer)

        content = JBScrollPane(table)
        content.border = JBEmptyBorder(0)
    }
}
