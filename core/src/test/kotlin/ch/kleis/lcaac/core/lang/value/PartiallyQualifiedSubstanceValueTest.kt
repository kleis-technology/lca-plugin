/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.lang.value

import ch.kleis.lcaac.core.lang.fixture.PartiallyQualifiedSubstanceValueFixture
import ch.kleis.lcaac.core.lang.fixture.UnitValueFixture
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PartiallyQualifiedSubstanceValueTest {

    @Test
    fun equals_whenExactMatch() {
        // given
        val a = PartiallyQualifiedSubstanceValueFixture.propanol
        val b = a.copy()

        // then
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun equals_whenSameDimension() {
        // given
        val a = PartiallyQualifiedSubstanceValueFixture.propanol.copy(referenceUnit = UnitValueFixture.kg())
        val b = PartiallyQualifiedSubstanceValueFixture.propanol.copy(referenceUnit = UnitValueFixture.ton())

        // then
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun equals_whenDifferentDimensions() {
        // given
        val a = PartiallyQualifiedSubstanceValueFixture.propanol.copy(referenceUnit = UnitValueFixture.kg())
        val b = PartiallyQualifiedSubstanceValueFixture.propanol.copy(referenceUnit = UnitValueFixture.l())

        // then
        assertNotEquals(a, b)
    }
}
