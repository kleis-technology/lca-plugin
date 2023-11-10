package ch.kleis.lcaac.plugin.ui.toolwindow.shared

import javax.swing.table.DefaultTableCellRenderer

object QuantityRenderer : DefaultTableCellRenderer() {
    override fun setValue(value: Any?) {
        when (value) {
            is Double -> text = FloatingPointRepresentation.of(value).toString()
            else -> super.setValue(value)
        }
    }

    // DefaultTableCellRenderer implements Serializable
    // https://blog.stylingandroid.com/kotlin-serializable-objects/ (2023-10-31)
    private fun readResolve(): Any = QuantityRenderer
}