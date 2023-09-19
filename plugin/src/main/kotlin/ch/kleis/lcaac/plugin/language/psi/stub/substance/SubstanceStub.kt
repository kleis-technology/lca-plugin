package ch.kleis.lcaac.plugin.language.psi.stub.substance

import ch.kleis.lcaac.plugin.psi.LcaSubstance
import com.intellij.psi.stubs.StubElement

interface SubstanceStub : StubElement<LcaSubstance> {
    val key: SubstanceKey
}
