package ch.kleis.lcaplugin.core.matrix

import ch.kleis.lcaplugin.core.lang_obsolete.VCharacterizationFactor
import ch.kleis.lcaplugin.core.lang_obsolete.VExchange
import ch.kleis.lcaplugin.core.lang_obsolete.VProduct
import ch.kleis.lcaplugin.core.lang_obsolete.VQuantity
import ch.kleis.lcaplugin.core.matrix.impl.Matrix


sealed interface InventoryResult

class InventoryError(val message: String) : InventoryResult
class InventoryMatrix(
    val observableProducts: IndexedCollection<VProduct>,
    val controllableProducts: IndexedCollection<VProduct>,
    private val data: Matrix
) : InventoryResult {
    fun value(outputProduct: VProduct, inputProduct: VProduct): VCharacterizationFactor {
        val outputUnit = outputProduct.dimensionDefaultUnit()
        val output = VExchange(VQuantity(1.0, outputUnit), outputProduct)

        val inputUnit = inputProduct.dimensionDefaultUnit()
        val amount = data.value(
            observableProducts.indexOf(outputProduct),
            controllableProducts.indexOf(inputProduct),
        )
        val input = VExchange(VQuantity(amount, inputUnit), inputProduct)

        return VCharacterizationFactor(output, input)
    }
}
