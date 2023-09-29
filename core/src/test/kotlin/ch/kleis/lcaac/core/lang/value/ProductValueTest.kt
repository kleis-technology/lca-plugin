/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.lang.value

import ch.kleis.lcaac.core.lang.fixture.ProductValueFixture
import ch.kleis.lcaac.core.lang.fixture.UnitValueFixture
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals


class ProductValueTest {
    @Test
    fun equals_whenReferenceUnitHaveSameDimension() {
        // given
        val a = ProductValueFixture.carrot.copy(referenceUnit = UnitValueFixture.kg())
        val b = ProductValueFixture.carrot.copy(referenceUnit = UnitValueFixture.ton())

        // then
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun equals_whenReferenceUnitHaveDifferentDimensions() {
        // given
        val a = ProductValueFixture.carrot.copy(referenceUnit = UnitValueFixture.kg())
        val b = ProductValueFixture.carrot.copy(referenceUnit = UnitValueFixture.l())

        // then
        assertNotEquals(a, b)
    }

    @Test
    fun equals_whenExactCopy() {
        // given
        val a = ProductValueFixture.carrot
        val b = a.copy()

        // then
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }
}
