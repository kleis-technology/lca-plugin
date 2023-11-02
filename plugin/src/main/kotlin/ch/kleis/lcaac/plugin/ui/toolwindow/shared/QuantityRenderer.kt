package ch.kleis.lcaac.plugin.ui.toolwindow.shared

import java.text.DecimalFormat
import javax.swing.table.DefaultTableCellRenderer

object QuantityRenderer : DefaultTableCellRenderer() {
    private val formatter = DecimalFormat("0.##E0")

    fun formatDouble(value: Double): String =
        formatter.format(value + 0.0).removeSuffix("E0")

    override fun setValue(value: Any?) {
        when (value) {
            is Double -> text = formatDouble(value)
            else -> super.setValue(value)
        }
    }

    // DefaultTableCellRenderer implements Serializable
    // https://blog.stylingandroid.com/kotlin-serializable-objects/ (2023-10-31)
    private fun readResolve(): Any = QuantityRenderer
}