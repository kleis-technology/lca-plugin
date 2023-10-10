package ch.kleis.lcaac.plugin.language.psi.stub.run

import ch.kleis.lcaac.plugin.psi.LcaRun
import ch.kleis.lcaac.plugin.psi.LcaTest
import com.intellij.psi.stubs.StubElement

interface RunStub : StubElement<LcaRun> {
    val fqn : String
}
