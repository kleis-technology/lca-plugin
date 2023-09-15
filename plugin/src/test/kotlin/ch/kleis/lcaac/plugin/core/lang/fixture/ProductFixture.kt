package ch.kleis.lcaac.plugin.core.lang.fixture

import ch.kleis.lcaac.plugin.core.lang.expression.EProductSpec

class ProductFixture {
    companion object {
        val carrot = EProductSpec(
            "carrot",
            QuantityFixture.oneKilogram,
        )
        val salad = EProductSpec(
            "salad",
            QuantityFixture.oneKilogram,
        )
        val water = EProductSpec(
            "water",
            QuantityFixture.oneLitre,
        )
    }
}
