package ch.kleis.lcaac.plugin.language.psi.stub.output_product

import ch.kleis.lcaac.plugin.psi.LcaOutputProductSpec
import com.intellij.psi.stubs.StubElement

interface OutputProductStub : StubElement<LcaOutputProductSpec> {
    val fqn: String
}
