package ch.kleis.lcaac.plugin.language.psi.stub.process

import ch.kleis.lcaac.plugin.psi.LcaProcess
import com.intellij.psi.stubs.StubElement

interface ProcessStub : StubElement<LcaProcess> {
    val key: ProcessKey
}
