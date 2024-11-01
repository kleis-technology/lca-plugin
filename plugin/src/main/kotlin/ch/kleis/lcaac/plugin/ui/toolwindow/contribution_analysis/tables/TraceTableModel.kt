package ch.kleis.lcaac.plugin.ui.toolwindow.contribution_analysis.tables

import ch.kleis.lcaac.core.assessment.ContributionAnalysis
import ch.kleis.lcaac.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.basic.BasicMatrix
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.plugin.MyBundle
import javax.swing.event.TableModelListener
import javax.swing.table.TableModel

class TraceTableModel(
    private val analysis: ContributionAnalysis<BasicNumber, BasicMatrix>,
    private val trace: EvaluationTrace<BasicNumber>,
    products: List<MatrixColumnIndex<BasicNumber>> = analysis.getObservablePorts().getElements(),
    private val requestedProducts: List<ProductValue<BasicNumber>> = analysis.entryPoint
        .products
        .map { it.product },
) : TableModel {
    private val products: List<MatrixColumnIndex<BasicNumber>> = products.sortedWith(trace.getComparator())
    private val displayTotal = requestedProducts.size > 1

    // 6 columns + 1 if more than one product: name, process, params, labels, unit, [total]
    private val columnPrefix = if (displayTotal) 7 else 6
    private val indicators: List<MatrixColumnIndex<BasicNumber>> = analysis.getControllablePorts().getElements()
        .sortedBy { it.getShortName() }

    override fun getRowCount(): Int {
        return products.size
    }

    override fun getColumnCount(): Int {
        return columnPrefix + requestedProducts.size + indicators.size
    }

    override fun getColumnName(columnIndex: Int): String {
        return when {
            columnIndex == 0 -> MyBundle.message("lca.results.tables.columns.depth")
            columnIndex == 1 -> MyBundle.message("lca.results.tables.columns.name")
            columnIndex == 2 -> MyBundle.message("lca.results.tables.columns.process_or_type")
            columnIndex == 3 -> MyBundle.message("lca.results.tables.columns.params_or_compartment")
            columnIndex == 4 -> MyBundle.message("lca.results.tables.columns.labels_or_subcompartment")
            columnIndex == 5 -> MyBundle.message("lca.results.tables.columns.unit")
            displayTotal && columnIndex == 6 -> MyBundle.message("lca.results.tables.columns.total")
            else -> {
                val offset = columnIndex - columnPrefix
                when {
                    offset < requestedProducts.size -> requestedProducts[offset].name
                    else -> indicators[offset - requestedProducts.size].getShortName()
                }
            }
        }
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        return when {
            columnIndex == 0 -> Int::class.java
            columnIndex <= 5 || (displayTotal && columnIndex == 6) -> String::class.java
            else -> Double::class.java
        }
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    @Suppress("DuplicatedCode")
    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val product = products[rowIndex]
        return when {
            columnIndex == 0 -> trace.getDepthOf(product) ?: -1
            columnIndex == 1 -> when (product) {
                is IndicatorValue -> product.getShortName()
                is ProductValue -> product.getShortName()
                is FullyQualifiedSubstanceValue -> product.getShortName()
                is PartiallyQualifiedSubstanceValue -> TODO()
            }

            columnIndex == 2 -> return when (product) {
                is IndicatorValue -> ""
                is ProductValue -> product.fromProcessRef?.name ?: ""
                is FullyQualifiedSubstanceValue -> product.type.value
                is PartiallyQualifiedSubstanceValue -> ""
            }

            columnIndex == 3 -> return when (product) {
                is IndicatorValue -> ""
                is ProductValue -> product.fromProcessRef?.renderArguments() ?: ""
                is FullyQualifiedSubstanceValue -> product.compartment
                is PartiallyQualifiedSubstanceValue -> ""
            }

            columnIndex == 4 -> return when (product) {
                is IndicatorValue -> ""
                is ProductValue -> product.fromProcessRef?.renderLabels() ?: ""
                is FullyQualifiedSubstanceValue -> product.subcompartment ?: ""
                is PartiallyQualifiedSubstanceValue -> ""
            }

            columnIndex == 5 -> {
                val total = analysis.supplyOf(product)
                "${total.unit.symbol}"
            }

            displayTotal && columnIndex == 6 -> {
                val total = analysis.supplyOf(product)
                total.amount.value
            }

            else -> {
                val offset = columnIndex - columnPrefix
                when {
                    offset < requestedProducts.size -> {
                        val requestedProduct = requestedProducts[columnIndex - columnPrefix]
                        val quantity = analysis.allocatedSupplyOf(product, requestedProduct)
                        quantity.amount.value
                    }

                    else -> {
                        val indicator = indicators[offset - requestedProducts.size]
                        val quantity = analysis.getPortContribution(product, indicator)
                        quantity.amount.value
                    }
                }
            }
        }
    }

    private fun FromProcessRefValue<BasicNumber>.renderArguments(): String {
        return this.arguments.entries.joinToString { "${it.key}=${it.value}" }
    }

    private fun FromProcessRefValue<BasicNumber>.renderLabels(): String {
        return this.matchLabels.entries.joinToString { "${it.key}=${it.value}" }
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
    }

    override fun addTableModelListener(l: TableModelListener?) {
    }

    override fun removeTableModelListener(l: TableModelListener?) {
    }
}
