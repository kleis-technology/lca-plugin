package ch.kleis.lcaac.plugin.language.psi.stub.test

import ch.kleis.lcaac.plugin.psi.LcaTest
import ch.kleis.lcaac.plugin.psi.LcaTypes
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class TestStubImpl(
    parent: StubElement<LcaTest>,
    override val fqn: String,
) : StubBase<LcaTest>(
    parent,
    LcaTypes.TEST as IStubElementType<out StubElement<*>, *>
), TestStub
