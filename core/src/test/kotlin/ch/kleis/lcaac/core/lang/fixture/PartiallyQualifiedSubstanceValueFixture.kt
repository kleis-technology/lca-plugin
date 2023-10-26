package ch.kleis.lcaac.core.lang.fixture

import ch.kleis.lcaac.core.lang.expression.EPackage
import ch.kleis.lcaac.core.lang.value.PackageValue
import ch.kleis.lcaac.core.lang.value.PartiallyQualifiedSubstanceValue
import ch.kleis.lcaac.core.math.basic.BasicNumber

class PartiallyQualifiedSubstanceValueFixture {
    companion object {
        val propanol =
            PartiallyQualifiedSubstanceValue(
                "propanol",
                UnitValueFixture.kg<BasicNumber>(),
                PackageValue(EPackage.DEFAULT_PKG_NAME)
            )
    }
}
