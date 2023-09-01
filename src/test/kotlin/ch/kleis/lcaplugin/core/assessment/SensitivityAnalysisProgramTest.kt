package ch.kleis.lcaplugin.core.assessment

import ch.kleis.lcaplugin.core.ParameterName
import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.dimension.UnitSymbol
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.value.ProcessValue
import ch.kleis.lcaplugin.core.lang.value.QuantityValue
import ch.kleis.lcaplugin.core.lang.value.SystemValue
import ch.kleis.lcaplugin.core.lang.value.UnitValue
import ch.kleis.lcaplugin.core.math.dual.DualNumber
import ch.kleis.lcaplugin.core.math.dual.DualOperations
import ch.kleis.lcaplugin.core.matrix.IndexedCollection
import ch.kleis.lcaplugin.core.matrix.ParameterVector
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.test.assertEquals

class SensitivityAnalysisProgramTest {

    @Test
    fun run_when1000Processes_shouldThrow() {
        // given
        val ops = DualOperations(1)
        val system = SystemValue(
            processes = IntRange(1, 1000).map {
                ProcessValue<DualNumber>(
                    name = "$it"
                )
            }.toSet()
        )
        val targetProcess = system.processes.first()
        val parameters = ParameterVector(
            names = IndexedCollection(listOf(ParameterName("p"))),
            data = listOf(
                QuantityValue(
                    ops.pure(1.0),
                    UnitValue(UnitSymbol.of("kg"), 1.0, Dimension.Companion.of("mass")),
                )
            )
        )
        val program = SensitivityAnalysisProgram(system, targetProcess, parameters)

        // when/then
        val e = assertThrows(EvaluatorException::class.java) {
            program.run()
        }
        assertEquals(
            "The current software version cannot perform the sensitivity analysis of a system with 1'000+ processes",
            e.message
        )
    }

    @Test
    fun run_whenNoParameters_shouldThrow() {
        // given
        val ops = DualOperations(1)
        val system = SystemValue<DualNumber>(
            processes = setOf(ProcessValue("p"))
        )
        val targetProcess = system.processes.first()
        val parameters = ParameterVector<DualNumber>(
            names = IndexedCollection(emptyList()),
            data = emptyList(),
        )
        val program = SensitivityAnalysisProgram(system, targetProcess, parameters)

        // when/then
        val e = assertThrows(EvaluatorException::class.java) {
            program.run()
        }
        assertEquals(
            "No quantitative parameter found",
            e.message
        )
    }
}
