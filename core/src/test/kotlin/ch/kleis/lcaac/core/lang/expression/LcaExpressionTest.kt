/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.lang.expression

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LcaExpressionTest {
    @Test
    fun test_SubstanceType_ShouldConvertByName() {
        // Given
        val name = "Emission"

        // When
        val sub = SubstanceType.of(name)

        // Then
        assertEquals(SubstanceType.EMISSION, sub)
    }

    @Test
    fun test_SubstanceType_ShouldFailWithInvalidName() {
        // Given
        val name = "Bad"

        // When / then
        val e = assertFailsWith(EvaluatorException::class, null) { SubstanceType.of(name) }
        assertEquals("Invalid SubstanceType: Bad", e.message)
    }

}
