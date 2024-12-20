package ch.kleis.lcaac.plugin.fixture

import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.math.basic.BasicOperations

object QuantityFixture {
    private val ops = BasicOperations
    val oneUnit = EQuantityScale(ops.pure(1.0), UnitFixture.unit)
    val oneKilogram = EQuantityScale(ops.pure(1.0), UnitFixture.kg)
    val oneGram = EQuantityScale(ops.pure(1.0), UnitFixture.g)
    val twoKilograms = EQuantityScale(ops.pure(2.0), UnitFixture.kg)
    val oneLitre = EQuantityScale(ops.pure(1.0), UnitFixture.l)
    val twoLitres = EQuantityScale(ops.pure(2.0), UnitFixture.l)
}
