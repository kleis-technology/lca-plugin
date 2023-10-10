package ch.kleis.lcaac.plugin.language.psi.stub.run

import ch.kleis.lcaac.plugin.psi.LcaRun
import ch.kleis.lcaac.plugin.psi.LcaTest
import ch.kleis.lcaac.plugin.psi.LcaTypes
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class RunStubImpl(
    parent: StubElement<LcaRun>,
    override val fqn: String,
) : StubBase<LcaRun>(
    parent,
    LcaTypes.RUN as IStubElementType<out StubElement<*>, *>
), RunStub
