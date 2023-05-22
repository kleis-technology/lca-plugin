package ch.kleis.lcaplugin.language.psi.stub

import ch.kleis.lcaplugin.language.psi.stub.substance.SubstanceKey
import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import ch.kleis.lcaplugin.psi.LcaSubstance
import ch.kleis.lcaplugin.psi.LcaTechnoProductExchange
import com.intellij.psi.stubs.StubIndexKey

object LcaStubIndexKeys {
    val SUBSTANCES: StubIndexKey<SubstanceKey, LcaSubstance> =
        StubIndexKey.createIndexKey("lca.substances")

    val TECHNO_PRODUCT_EXCHANGES: StubIndexKey<String, LcaTechnoProductExchange> =
        StubIndexKey.createIndexKey("lca.technoProductExchanges")

    val PROCESSES: StubIndexKey<String, PsiProcess> =
        StubIndexKey.createIndexKey("lca.processes")

    val UNITS: StubIndexKey<String, PsiUnitDefinition> =
        StubIndexKey.createIndexKey("lca.units")

    val GLOBAL_ASSIGNMENTS: StubIndexKey<String, PsiGlobalAssignment> =
        StubIndexKey.createIndexKey("lca.globalAssignments")
}
