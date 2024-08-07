package ch.kleis.lcaac.plugin.fixture

import ch.kleis.lcaac.core.lang.expression.*

class TemplateFixture {
    companion object {
        val carrotProduction = EProcessTemplate(
            params = mapOf(
                Pair("q_water", QuantityFixture.oneLitre)
            ),
            locals = mapOf(
                Pair("q_carrot", QuantityFixture.oneKilogram)
            ),
            body = EProcess(
                name = "carrot_production",
                products = listOf(
                    ETechnoExchange(EDataRef("q_carrot"), ProductFixture.carrot),
                ),
                inputs = listOf(
                    ETechnoBlockEntry(
                        ETechnoExchange(EDataRef("q_water"), ProductFixture.water),
                    )
                ),
            )
        )
        val cyclicProduction = EProcessTemplate(
            params = mapOf(
                Pair("q_water", QuantityFixture.oneLitre)
            ),
            body = EProcess(
                name = "carrot_production",
                products = listOf(
                    ETechnoExchange(QuantityFixture.twoKilograms, ProductFixture.carrot),
                ),
                inputs = listOf(
                    ETechnoBlockEntry(
                        ETechnoExchange(QuantityFixture.oneGram, ProductFixture.carrot),
                    )
                ),
            )
        )
        val withUnboundedRef = EProcessTemplate(
            body = EProcess(
                name = "with_unbounded_ref",
                products = listOf(
                    ETechnoExchange(EDataRef("q_carrot"), ProductFixture.carrot),
                ),
                inputs = listOf(
                    ETechnoBlockEntry(
                        ETechnoExchange(EDataRef("q_water"), ProductFixture.water),
                    )
                ),
            )
        )
    }
}
