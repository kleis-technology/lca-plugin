package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.*
import com.intellij.testFramework.assertEqualsToFile
import org.junit.Assert.assertEquals
import org.junit.Test


class EvaluatorTest {

    @Test
    fun eval() {
        // given
        val kg = EUnit("kg", 1.0, Dimension.of("mass"))
        val l = EUnit("l", 1.0, Dimension.of("volume"))
        val carrot = EProduct("carrot", Dimension.of("mass"), kg)
        val water = EProduct("water", Dimension.of("volume"), l)
        val expression = ESystem(
            listOf(
                EProcess(
                    listOf(
                        EExchange(EQuantity(1.0, kg), carrot),
                        EBlock(
                            listOf(
                                EExchange(EQuantity(3.0, l), water),
                            ), Polarity.NEGATIVE
                        )
                    )
                )
            )
        )
        val evaluator = Evaluator(emptyMap())

        // when
        val actual = evaluator.eval(expression)

        // then
        val vKg = VUnit("kg", 1.0, Dimension.of("mass"))
        val vL = VUnit("l", 1.0, Dimension.of("volume"))
        val vCarrot = VProduct("carrot", Dimension.of("mass"), vKg)
        val vWater = VProduct("water", Dimension.of("volume"), vL)
        val expected = VSystem(
            listOf(
                VProcess(
                    listOf(
                        VExchange(VQuantity(1.0, vKg), vCarrot),
                        VExchange(VQuantity(-3.0, vL), vWater),
                    )
                )
            )
        )
        assertEquals(expected, actual)
    }
}