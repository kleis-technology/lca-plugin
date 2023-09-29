/** 
* Copyright (C) Kleis Technology Sàrl - All Rights Reserved 
* Unauthorized copying of this file, via any medium is strictly prohibited 
* Proprietary and confidential 
* Written by Arnaud Béguin <abeguin@kleis.ch>, September 2023 
*/ 

package ch.kleis.lcaac.core.lang.dimension

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class DimensionTest {

    @Test
    fun test_toString_whenNone() {
        // given
        val dimension = Dimension.None

        // when
        val actual = "$dimension"

        // then
        assertEquals("none", actual)
    }

    @Test
    fun test_toString_whenSimpleDim() {
        // given
        val dimension = Dimension.of("something")

        // when
        val actual = "$dimension"

        // then
        assertEquals("something", actual)
    }

    @Test
    fun test_toString_whenSmallPower() {
        // given
        val dimension = Dimension.of("something", 2)

        // when
        val actual = "$dimension"

        // then
        assertEquals("something²", actual)
    }
}
