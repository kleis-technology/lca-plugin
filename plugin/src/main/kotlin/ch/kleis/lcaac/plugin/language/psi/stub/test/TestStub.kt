package ch.kleis.lcaac.plugin.language.psi.stub.test

import ch.kleis.lcaac.plugin.psi.LcaTest
import com.intellij.psi.stubs.StubElement

interface TestStub : StubElement<LcaTest> {
    val fqn : String
}
