package ch.kleis.lcaac.plugin.language.psi.stub.process

import ch.kleis.lcaac.plugin.psi.LcaProcess
import ch.kleis.lcaac.plugin.psi.LcaTypes
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class ProcessStubImpl(
    parent: StubElement<LcaProcess>,
    override val key: ProcessKey,
) :
    StubBase<LcaProcess>(parent, LcaTypes.PROCESS as IStubElementType<out StubElement<*>, *>),
    ProcessStub
