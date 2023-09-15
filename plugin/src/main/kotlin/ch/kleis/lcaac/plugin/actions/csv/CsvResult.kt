package ch.kleis.lcaac.plugin.actions.csv

import ch.kleis.lcaac.plugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaac.plugin.core.lang.value.QuantityValue
import ch.kleis.lcaac.plugin.core.math.basic.BasicNumber

data class CsvResult(
    val request: CsvRequest,
    val output: MatrixColumnIndex<BasicNumber>,
    val impacts: Map<MatrixColumnIndex<BasicNumber>, QuantityValue<BasicNumber>>,
)
