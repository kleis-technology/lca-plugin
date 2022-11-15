package ch.kleis.lcaplugin.compute.matrix

import ch.kleis.lcaplugin.compute.matrix.impl.Matrix
import ch.kleis.lcaplugin.compute.matrix.impl.MatrixFactory
import ch.kleis.lcaplugin.compute.model.CharacterizationFactor
import ch.kleis.lcaplugin.compute.model.ElementaryFlow
import ch.kleis.lcaplugin.compute.model.Indicator
import java.util.stream.Collectors.toMap

class BioFactorMatrix(
    private val elementaryFlows: IndexedCollection<ElementaryFlow<*>>,
    private val indicators: IndexedCollection<Indicator<*>>,
    private val characterizationFactors: List<CharacterizationFactor>,
) {
    val matrix: Matrix = MatrixFactory.INSTANCE.zero(elementaryFlows.size(), indicators.size())

    init {
        val cfs = characterizationFactors.stream()
            .collect(toMap(
                { cf -> Pair(cf.input.flow, cf.output.flow) },
                { cf -> cf.input.quantity.divide(cf.output.quantity) },
            ))
       elementaryFlows.getElements()
           .forEach { flow ->
               val row = elementaryFlows.indexOf(flow)
               indicators.getElements()
                   .forEach { indicator ->
                       val col = indicators.indexOf(indicator)
                       val factor = cfs[Pair(indicator, flow)] ?: throw NoSuchElementException()
                       matrix.add(row, col, factor.referenceValue())
                   }
           }
    }
}