package ch.kleis.lcaac.plugin.language.psi.stub.unit

import ch.kleis.lcaac.plugin.psi.LcaUnitDefinition
import com.intellij.psi.stubs.StubElement

interface UnitStub : StubElement<LcaUnitDefinition> {
    val fqn: String
}
