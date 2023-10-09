package ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.issues

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.value.FullyQualifiedSubstanceValue
import ch.kleis.lcaac.core.lang.value.PartiallyQualifiedSubstanceValue
import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.lang.value.SubstanceValue
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.ui.toolwindow.FloatingPointRepresentation
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class NonCharacterizedSubstanceTableModel(
    private val analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
    private val requestedProducts: List<ProductValue<BasicNumber>> = analysis.getEntryPoint()
        .products
        .map { it.product },
    unresolvedSubstances: List<SubstanceValue<BasicNumber>> = analysis.getControllableSubstances(),
) : TableModel {
    private val unresolvedSubstances = unresolvedSubstances.sortedBy { it.getUID() }
    override fun getRowCount(): Int {
        return unresolvedSubstances.size
    }

    override fun getColumnCount(): Int {
        return 5 + requestedProducts.size
    }

    override fun getColumnName(columnIndex: Int): String {
        if (columnIndex == 0) return "type"
        if (columnIndex == 1) return "name"
        if (columnIndex == 2) return "compartment"
        if (columnIndex == 3) return "sub_compartment"
        if (columnIndex == 4) return "unit"
        val requestedProduct = requestedProducts[columnIndex - 5]
        return "${requestedProduct.name} [${requestedProduct.referenceUnit.symbol}]"
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        return if (columnIndex <= 4) String::class.java
        else FloatingPointRepresentation::class.java
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val substance = unresolvedSubstances[rowIndex]
        if (columnIndex == 0) return substance.type()
        if (columnIndex == 1) return substance.name()
        if (columnIndex == 2) return substance.compartment()
        if (columnIndex == 3) return substance.subCompartment()
        if (columnIndex == 4) return "${substance.referenceUnit().symbol}"
        val offset = columnIndex - 5
        val requestedProduct = requestedProducts[offset]
        val contribution = analysis.getPortContribution(requestedProduct, substance)
        return FloatingPointRepresentation.of(contribution.amount.value)
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
    }

    override fun addTableModelListener(l: TableModelListener?) {
    }

    override fun removeTableModelListener(l: TableModelListener?) {
    }

    private fun SubstanceValue<BasicNumber>.name(): String = when(this) {
        is FullyQualifiedSubstanceValue -> this.name
        is PartiallyQualifiedSubstanceValue -> this.name
    }

    private fun SubstanceValue<BasicNumber>.type(): String = when(this) {
        is FullyQualifiedSubstanceValue -> this.type.value
        is PartiallyQualifiedSubstanceValue -> ""
    }

    private fun SubstanceValue<BasicNumber>.compartment(): String = when(this) {
        is FullyQualifiedSubstanceValue -> this.compartment
        is PartiallyQualifiedSubstanceValue -> ""
    }

    private fun SubstanceValue<BasicNumber>.subCompartment(): String = when(this) {
        is FullyQualifiedSubstanceValue -> this.subcompartment ?: ""
        is PartiallyQualifiedSubstanceValue -> ""
    }

}
